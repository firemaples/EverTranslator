package tw.firemaples.onscreenocr.recognition

import android.graphics.Bitmap
import android.graphics.Rect

interface TextRecognizer {
    companion object {
        fun getRecognizer(): TextRecognizer {
            //TODO implement TessTwo recognizer
            return GoogleMLKitTextRecognizer()
        }
    }

    val type: Recognizer
    val name: String
    suspend fun recognize(bitmap: Bitmap): RecognitionResult
    suspend fun supportedLanguages(): List<RecognitionLanguage>
    suspend fun parseToDisplayLangCode(langCode: String): String
}

enum class Recognizer {
    TessTwo,
    GoogleMLKit,
}

data class RecognitionLanguage(
    val code: String,
    val displayName: String,
    val selected: Boolean
)

data class RecognitionResult(
    val langCode: String,
    val result: String,
    val boundingBoxes: List<Rect>
)
