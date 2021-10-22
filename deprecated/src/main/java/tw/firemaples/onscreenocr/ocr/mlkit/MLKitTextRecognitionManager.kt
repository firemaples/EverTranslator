package tw.firemaples.onscreenocr.ocr.mlkit

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognizerOptions
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.ocr.TextRecognitionManager

object MLKitTextRecognitionManager : TextRecognitionManager.ITextRecognitionEngine {
    override val type: TextRecognitionManager.RecognitionEngine =
            TextRecognitionManager.RecognitionEngine.MLKitTextRecognition

    private val recognizer: TextRecognizer by lazy {
        val options = TextRecognizerOptions.Builder().build()
        TextRecognition.getClient(options)
    }

    override fun recognize(croppedBitmap: Bitmap, lang: String, failed: ((Throwable) -> Unit)?, success: (text: String, textBoxes: List<Rect>) -> Unit) {
        val image = InputImage.fromBitmap(croppedBitmap, 0)

        recognizer.process(image).addOnSuccessListener { result ->
            val text = result.text
            val textBoxes = result.textBlocks.mapNotNull { it.boundingBox }

            FirebaseEvent.logStartOCR(name)
            success(text, textBoxes)
        }.addOnFailureListener {
            FirebaseEvent.logOCRFailed(name, it)
            failed?.invoke(it)
        }
    }
}
