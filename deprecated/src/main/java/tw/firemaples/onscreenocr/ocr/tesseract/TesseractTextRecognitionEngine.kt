package tw.firemaples.onscreenocr.ocr.tesseract

import android.graphics.Bitmap
import android.graphics.Rect
import com.googlecode.tesseract.android.TessBaseAPI
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.ocr.TextRecognitionManager
import tw.firemaples.onscreenocr.utils.SettingUtil
import tw.firemaples.onscreenocr.utils.Utils
import java.io.File

object TesseractTextRecognitionEngine : TextRecognitionManager.ITextRecognitionEngine {
    override val type: TextRecognitionManager.RecognitionEngine =
            TextRecognitionManager.RecognitionEngine.TesseractOCR

    private val baseAPI: TessBaseAPI by lazy { TessBaseAPI() }

    private val tessRootDir: File
        get() = OCRFileUtil.tessDataBaseDir

    private val tessPageMode: Int
        get() = OCRLangUtil.pageSegmentationModeIndex

    private fun initialize(lang: String) {
        FirebaseEvent.logStartOCRInitializing(name)
        baseAPI.init(tessRootDir.absolutePath, lang, TessBaseAPI.OEM_DEFAULT)
        baseAPI.pageSegMode = tessPageMode
        FirebaseEvent.logOCRInitialized(name)
    }

    override fun recognize(croppedBitmap: Bitmap, lang: String, failed: ((Throwable) -> Unit)?, success: (text: String, textBoxes: List<Rect>) -> Unit) {
        initialize(lang)

        try {
            FirebaseEvent.logStartOCR(name)

            baseAPI.setImage(croppedBitmap)
            var resultText = baseAPI.utF8Text ?: ""

            if (SettingUtil.removeLineBreaks) {
                resultText = Utils.replaceAllLineBreaks(resultText, " ") ?: ""
            }

            val textBoxes = baseAPI.regions.boxRects

            FirebaseEvent.logOCRFinished(name)
            success(resultText, textBoxes)
        } catch (t: Throwable) {
            FirebaseEvent.logOCRFailed(name, t)
            failed?.invoke(t)
        }
    }
}
