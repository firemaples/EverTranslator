package tw.firemaples.onscreenocr.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.ocr.mlkit.MLKitTextRecognitionManager
import tw.firemaples.onscreenocr.ocr.tesseract.TesseractTextRecognitionEngine
import tw.firemaples.onscreenocr.utils.threadIO
import java.io.File

object TextRecognitionManager {
    private val logger: Logger by lazy { LoggerFactory.getLogger(TextRecognitionManager::class.java) }

    private val latinBasedLanguages: List<String> = listOf("eng")

    private fun getTextRecognitionEngine(lang: String): ITextRecognitionEngine =
            if (latinBasedLanguages.contains(lang)) MLKitTextRecognitionManager
            else TesseractTextRecognitionEngine

    private fun getTextRecognitionEngineFallback(current: ITextRecognitionEngine): ITextRecognitionEngine? =
            if (current is MLKitTextRecognitionManager) TesseractTextRecognitionEngine else null

    fun recognize(imageFile: File, userSelectedRect: Rect, lang: String, onSuccess: (text: String, textBoxes: List<Rect>) -> Unit, onFailed: (Throwable) -> Unit) {
        threadIO {
            val fullBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            val croppedBitmap = Bitmap.createBitmap(fullBitmap, userSelectedRect.left, userSelectedRect.top, userSelectedRect.width(), userSelectedRect.height())
            fullBitmap.recycle()
            val engine = getTextRecognitionEngine(lang)
            logger.debug("Start text recognition with engine: ${engine.name}")
            engine.recognize(
                    croppedBitmap = croppedBitmap,
                    lang = lang,
                    success = { text, textBoxes ->
                        croppedBitmap.recycle()
                        logger.debug("Text recognition success, text: $text, textBoxes: $textBoxes")
                        onSuccess(text, textBoxes)
                    },
                    failed = { throwable ->
                        logger.warn("Text recognition failed", throwable)
                        FirebaseEvent.logOCRFailed(engine = engine.name, throwable)
                        val fallback = getTextRecognitionEngineFallback(engine)
                        if (fallback != null) {
                            logger.debug("Fall back to $fallback")
                            FirebaseEvent.logOCRFallback(from = engine.name,
                                    to = fallback.name)
                            fallback.recognize(
                                    croppedBitmap = croppedBitmap,
                                    lang = lang,
                                    success = { text, textBoxes ->
                                        croppedBitmap.recycle()
                                        logger.debug("Text recognition success by fallback")
                                        onSuccess(text, textBoxes)
                                    }, failed = { t ->
                                logger.warn("Text recognition failed by fallback", t)
                                FirebaseEvent.logOCRFailed(engine = fallback.name, t)
                                onFailed(t)
                            })
                        } else {
                            croppedBitmap.recycle()
                            onFailed(throwable)
                        }
                    }
            )
        }
    }

    interface ITextRecognitionEngine {
        val type: RecognitionEngine
        val name: String
            get() = type.name

        fun recognize(croppedBitmap: Bitmap, lang: String, failed: ((Throwable) -> Unit)? = null, success: (text: String, textBoxes: List<Rect>) -> Unit)
    }

    enum class RecognitionEngine {
        MLKitTextRecognition, TesseractOCR
    }

//    interface TextRecognitionCallback {
//        fun initializing()
//        fun initialized()
//        fun onRecognizing()
//        fun onRecognized(text: String, textBoxes: List<Rect>)
//    }
}