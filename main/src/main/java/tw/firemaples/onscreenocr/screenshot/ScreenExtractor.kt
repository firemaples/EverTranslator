package tw.firemaples.onscreenocr.screenshot

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.utils.Constants
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.UIUtils
import tw.firemaples.onscreenocr.utils.Utils
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object ScreenExtractor {
    private val context: Context by lazy { Utils.context }
    private val logger: Logger by lazy { Logger(ScreenExtractor::class) }

    private var mediaProjectionIntent: Intent? = null

    val isGranted: Boolean
        get() = mediaProjectionIntent != null

    private val handler: Handler by lazy {
        Handler(HandlerThread("Thread-${ScreenExtractor::class.simpleName}").run {
            start()
            looper
        })
    }

    private var virtualDisplay: VirtualDisplay? = null
//    private val screenshotDir: File by lazy { File(context.cacheDir, Constants.PATH_SCREENSHOT) }
//    private val screenshotFile: File by lazy { File(screenshotDir, "screenshot.jpg") }

    private val screenDensityDpi: Int
        get() = UIUtils.displayMetrics.densityDpi

    fun onMediaProjectionGranted(intent: Intent) {
        mediaProjectionIntent = intent.clone() as Intent
    }

    fun release() {
        releaseAllResources()
        virtualDisplay?.release()
        mediaProjectionIntent = null
    }

    @Throws(
        IllegalStateException::class,
        IllegalArgumentException::class,
        TimeoutCancellationException::class
    )
    suspend fun extractBitmapFromScreen(parentRect: Rect, cropRect: Rect): Bitmap {
        logger.debug("extractBitmapFromScreen(), parentRect: $parentRect, cropRect: $cropRect")

        val fullBitmap = doCaptureScreen()

        return try {
            cropBitmap(fullBitmap, parentRect, cropRect)
        } finally {
            fullBitmap.recycle()
        }
    }

    private var projection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var image: Image? = null

    @SuppressLint("WrongConstant")
    @Throws(
        IllegalStateException::class,
        IllegalArgumentException::class,
        TimeoutCancellationException::class,
    )
    private suspend fun doCaptureScreen(): Bitmap {
        var bitmap: Bitmap
        withContext(Dispatchers.Default) {
            releaseAllResources()

            val mpIntent = mediaProjectionIntent
            if (mpIntent == null) {
                logger.warn("The mediaProjectionIntent is null: $mpIntent")
                throw IllegalStateException("The media projection intent is not initialized")
            }

            val mpManager =
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

            try {
                projection =
                    mpManager.getMediaProjection(Activity.RESULT_OK, mpIntent.clone() as Intent)

                val projection = projection
                if (projection == null) {
                    logger.warn("Retrieve projection failed, projection: $projection")
                    throw IllegalStateException("Retrieving media projection failed")
                }

                val size = UIUtils.readSize
                val width = size.x
                val height = size.y

                imageReader =
                    ImageReader.newInstance(width, height, AppPref.imageReaderFormat, 2)
                val imageReader = imageReader

                if (imageReader == null) {
                    logger.debug("The imageReader is null after initialized")
                    releaseAllResources()
                    throw IllegalStateException("No image reader initialized failed")
                }

                virtualDisplay = projection.createVirtualDisplay(
                    "screen-mirror",
                    width, height, screenDensityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    imageReader.surface, null, null
                )

                logger.debug("waitForImage")
                image = withTimeout(SettingManager.timeoutForCapturingScreen) {
                    imageReader.waitForImage()
                }

                virtualDisplay?.release()

                val image = image
                if (image == null) {
                    logger.warn("The captured image is null")
                    releaseAllResources()
                    throw IllegalStateException("No image data found")
                }

                bitmap = image.decodeBitmap(size)

                releaseAllResources()

                logger.debug("Bitmap size: ${bitmap.width}x${bitmap.height}, screen size: ${width}x$height")
            } catch (e: Throwable) {
                logger.warn(t = e)

                releaseAllResources()

                val message = e.message ?: e.localizedMessage
                if (message != null) {
                    val match = Constants.regexForImageReaderFormatError.find(message)
                    val formatValue = match?.groupValues?.get(1)?.toIntOrNull()
                    if (formatValue != null) {
                        val msg =
                            "Format not matched error found, change the image reader format from ${AppPref.imageReaderFormat} to $formatValue"
                        logger.warn(msg)
                        AppPref.imageReaderFormat = formatValue
                        FirebaseEvent.logException(Exception(msg, e))

                        throw Exception(context.getString(R.string.msg_image_reader_format_unmatched))
                    }
                }

                throw e
            }
        }

        return bitmap
    }

    private fun releaseAllResources() {
        try {
            imageReader?.setOnImageAvailableListener(null, handler)
        } catch (e: Exception) {
            FirebaseEvent.logException(e)
        }
        try {
            imageReader?.close()
        } catch (e: Exception) {
            FirebaseEvent.logException(e)
        }
        try {
            projection?.stop()
        } catch (e: Exception) {
            FirebaseEvent.logException(e)
        }
        try {
            image?.close()
        } catch (e: Exception) {
            FirebaseEvent.logException(e)
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun cropBitmap(bitmap: Bitmap, parentRect: Rect, cropRect: Rect): Bitmap {
        logger.debug(
            "cropBitmap(), " +
                    "bitmap: ${bitmap.width}x${bitmap.height}, " +
                    "parentRect: $parentRect, cropRect: $cropRect"
        )

        val top = parentRect.top + cropRect.top
        val bottom = parentRect.top + cropRect.bottom

        val left = parentRect.left + cropRect.left
        val right = parentRect.left + cropRect.right

        val rect = Rect(left, top, right, bottom)

        val width = rect.width().coerceAtMost(bitmap.width - rect.left)
        val height = rect.height().coerceAtMost(bitmap.height - rect.top)

        val cropped = Bitmap.createBitmap(bitmap, rect.left, rect.top, width, height)
        logger.debug("cropped bitmap: ${cropped.width}x${cropped.height}")

        return cropped
    }

    private suspend fun ImageReader.waitForImage(): Image? =
        suspendCoroutine {
            setOnImageAvailableListener({ reader ->
                try {
                    reader.setOnImageAvailableListener(null, null)
                    logger.info("onImageAvailable()")
                    val image = reader.acquireLatestImage()
                    it.resume(image)
                } catch (e: Exception) {
                    logger.warn(t = e)
                    it.resumeWithException(e)
                } finally {
                }
            }, handler)
        }

    @Throws(IllegalArgumentException::class)
    private fun Image.decodeBitmap(size: Point): Bitmap =
        with(planes[0]) {
            val width = size.x
            val height = size.y
//            val deviceWidth = screenWidth
//            val rowPadding = rowStride - pixelStride * deviceWidth
            val temp = Bitmap.createBitmap(
                rowStride / pixelStride,
//                screenWidth + rowPadding / pixelStride,
                height,
                Bitmap.Config.ARGB_8888
            ).apply {
                copyPixelsFromBuffer(buffer)
            }

            Bitmap.createBitmap(temp, 0, 0, width, height).also {
                temp.recycle()
            }
        }
}
