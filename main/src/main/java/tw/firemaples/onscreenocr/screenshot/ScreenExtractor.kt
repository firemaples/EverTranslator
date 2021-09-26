package tw.firemaples.onscreenocr.screenshot

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tw.firemaples.onscreenocr.utils.Constraints
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.UIUtils
import tw.firemaples.onscreenocr.utils.Utils
import java.io.File
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

    //    private val mediaRecorder: MediaRecorder = MediaRecorder()
    private var virtualDisplay: VirtualDisplay? = null
    private val screenshotDir: File by lazy { File(context.cacheDir, Constraints.PATH_SCREENSHOT) }
//    private val screenshotFile: File by lazy { File(screenshotDir, "screenshot.jpg") }

    private val screenWidth: Int
        get() = UIUtils.displayMetrics.widthPixels
    private val screenHeight: Int
        get() = UIUtils.displayMetrics.heightPixels
    private val screenDensityDpi: Int
        get() = UIUtils.displayMetrics.densityDpi

    init {
//        setupRecorder()
    }

    fun onMediaProjectionGranted(intent: Intent) {
        mediaProjectionIntent = intent.clone() as Intent
    }

    fun release() {
        virtualDisplay?.release()
        mediaProjectionIntent = null
    }

    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    suspend fun extractBitmapFromScreen(parentRect: Rect, cropRect: Rect): Bitmap? {
        logger.debug("extractBitmapFromScreen(), parentRect: $parentRect, cropRect: $cropRect")

        val fullBitmap = doCaptureScreen()
        if (fullBitmap == null) {
            logger.debug("The capture result is null: $fullBitmap")
            return null
        }

        return try {
            cropBitmap(fullBitmap, parentRect, cropRect)
        } finally {
            fullBitmap.recycle()
        }
    }

//    private fun setupRecorder() {
//        try {
//            with(mediaRecorder) {
//                //            setAudioSource(MediaRecorder.AudioSource.MIC)
//                setVideoSource(MediaRecorder.VideoSource.SURFACE)
//                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//
//                setOutputFile(screenshotMp4.absolutePath)
//                setVideoSize(screenWidth, screenHeight)
//                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//                //            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//                setVideoEncodingBitRate(512_000)
//                setVideoFrameRate(5)
//
//
//                setOrientationHint(UIUtils.orientationDegree)
//                prepare()
//            }
//        } catch (e: Exception) {
//            logger.error(t = e)
//        }
//    }

    @SuppressLint("WrongConstant")
    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    private suspend fun doCaptureScreen(): Bitmap? {
        var bitmap: Bitmap? = null
        withContext(Dispatchers.Default) {
            val mpIntent = mediaProjectionIntent
            if (mpIntent == null) {
                logger.warn("The mediaProjectionIntent is null: $mpIntent")
                throw IllegalStateException("The media projection intent is not initialized")
            }

            val mpManager =
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val projection =
                mpManager.getMediaProjection(Activity.RESULT_OK, mpIntent.clone() as Intent)

            if (projection == null) {
                logger.warn("Retrieve projection failed, projection: $projection")
                throw IllegalStateException("Retrieving media projection failed")
            }

            val imageReader =
                ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)

            virtualDisplay = projection.createVirtualDisplay(
                "screen-mirror",
                screenWidth, screenHeight, screenDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                imageReader.surface, null, null
            )

//        mediaRecorder.start()

//        delay(2000L)

            logger.debug("waitForImage")
            val image = imageReader.waitForImage()

//        mediaRecorder.stop()
//        mediaRecorder.reset()
            virtualDisplay?.release()

            if (image == null) {
                logger.warn("The captured image is null")
                imageReader.close()
                throw IllegalStateException("No image data found")
            }

            bitmap = image.decodeBitmap()

            image.close()
            imageReader.close()
            projection.stop()

            logger.debug("Bitmap size: ${bitmap?.width}x${bitmap?.height}, screen size: ${screenWidth}x$screenHeight")
        }

        return bitmap
    }

    @Throws(IllegalArgumentException::class)
    private fun cropBitmap(bitmap: Bitmap, parentRect: Rect, cropRect: Rect): Bitmap {
        logger.debug(
            "cropBitmap(), " +
                    "bitmap: ${bitmap.width}x${bitmap.height}, " +
                    "screen size: ${screenWidth}x$screenHeight, " +
                    "parentRect: $parentRect, cropRect: $cropRect"
        )

        val metric = UIUtils.realDisplayMetrics

        val widthScale = bitmap.width.toFloat() / metric.widthPixels.toFloat()
//        val widthScale = metric.widthPixels.toFloat() / bitmap.width.toFloat()
        val heightScale = bitmap.height.toFloat() / metric.heightPixels.toFloat()
//        val widthOffset = (bitmap.width.toFloat() - metric.widthPixels.toFloat()) / 2f
        val widthOffset = 0f

//        val startX = (parentRect.left + cropRect.left).times(widthScale).toInt()
//        val startY = (parentRect.top + cropRect.top).times(heightScale).toInt()
//        val width = cropRect.width().times(widthScale).toInt()
//        val height = cropRect.height().times(heightScale).toInt()

        val top = (parentRect.top + cropRect.top).times(heightScale).toInt()
        val bottom = (parentRect.top + cropRect.bottom).times(heightScale).toInt()

        val left =
            (parentRect.left + cropRect.left).plus(widthOffset).toInt() //.times(widthScale).toInt()
//            (parentRect.left + cropRect.left).times(widthScale).toInt() //.times(widthScale).toInt()
        val right =
            (parentRect.left + cropRect.right).plus(widthOffset).toInt()//.times(widthScale).toInt()
//            (parentRect.left + cropRect.right).times(widthScale).toInt()//.times(widthScale).toInt()

        val rect = Rect(left, top, right, bottom)

//        val cropped = Bitmap.createBitmap(bitmap, startX, startY, width, height)
        val cropped = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
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
//                    try {
//                        reader.close()
//                    } catch (e: Exception) {
//                        logger.warn(t = e)
//                    }
                }
            }, handler)
        }

    @Throws(IllegalArgumentException::class)
    private fun Image.decodeBitmap(): Bitmap =
        with(planes[0]) {
//            val deviceWidth = screenWidth
//            val rowPadding = rowStride - pixelStride * deviceWidth
            Bitmap.createBitmap(
                rowStride / pixelStride,
//                screenWidth + rowPadding / pixelStride,
                screenHeight,
                Bitmap.Config.ARGB_8888
            ).apply {
                copyPixelsFromBuffer(buffer)
            }
        }
}
