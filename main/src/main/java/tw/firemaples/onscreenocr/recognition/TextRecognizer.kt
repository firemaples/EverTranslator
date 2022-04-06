package tw.firemaples.onscreenocr.recognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.annotation.StringRes
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.utils.Constants
import tw.firemaples.onscreenocr.utils.Utils

interface TextRecognizer {
    companion object {
        private val context: Context by lazy { Utils.context }
        private var supportedLanguagesCache: List<RecognitionLanguage> = listOf()
        private val supportedLanguages: List<RecognitionLanguage>
            get() {
                if (supportedLanguagesCache.isEmpty())
                    supportedLanguagesCache = initializeSupportLanguages()
                return supportedLanguagesCache
            }
        private val recognizerCache = mutableMapOf<Recognizer, TextRecognizer>()

        fun getRecognizer(selectedLang: String): TextRecognizer {
            val selected = fixSelectLang(selectedLang)
            val langItem = supportedLanguages.first { it.code == selected }

            return recognizerCache.getOrPut(langItem.recognizer) {
                when (langItem.recognizer) {
                    Recognizer.GoogleMLKit -> GoogleMLKitTextRecognizer()
                    Recognizer.Tesseract -> TesseractTextRecognizer()
                }
            }
        }

        private fun initializeSupportLanguages(): List<RecognitionLanguage> {
            val mlKitLangList = GoogleMLKitTextRecognizer.getSupportedLanguageList(context)
            val tesseractLangList = TesseractTextRecognizer.getSupportedLanguageList(context)

            val langList = mlKitLangList.toMutableList()
            langList += tesseractLangList.filterNot { langList.any { lang -> lang.code == it.code } }

            return langList.sortedBy { it.displayName }
        }

        fun allSupportedLanguages(selectedLang: String): List<RecognitionLanguage> {
            val selected = fixSelectLang(selectedLang)

            return supportedLanguages.map {
                when {
                    it.code == selected && !it.selected -> it.copy(selected = true)
                    it.selected && it.code != selected -> it.copy(selected = false)
                    else -> it
                }
            }
        }

        fun invalidSupportLanguages() {
            supportedLanguagesCache = listOf()
        }

        fun getLanguage(lang: String): RecognitionLanguage? =
            supportedLanguages.firstOrNull { it.code == lang }

        private fun fixSelectLang(selectedLang: String): String =
            if (supportedLanguages.any { it.code == selectedLang }) selectedLang
            else Constants.DEFAULT_OCR_LANG
    }

    val type: Recognizer
    val name: String
    suspend fun recognize(lang: RecognitionLanguage, bitmap: Bitmap): RecognitionResult
//    suspend fun supportedLanguages(): List<RecognitionLanguage>
    suspend fun parseToDisplayLangCode(langCode: String): String
}

enum class TextRecognitionProviderType(
    val index: Int,
    val key: String,
    @StringRes
    val nameRes: Int,
) {
    GoogleMLKit(0, "google_ml_kit", R.string.ocr_provider_google_ml_kit),
    Tesseract(1, "tesseract", R.string.ocr_provider_tesseract),
}

enum class Recognizer {
    GoogleMLKit,
    Tesseract,
}

data class RecognitionLanguage(
    val code: String,
    val displayName: String,
    val selected: Boolean,
    val downloaded: Boolean,
    val recognizer: Recognizer,
    val innerCode: String,
)

data class RecognitionResult(
    val langCode: String,
    val result: String,
    val boundingBoxes: List<Rect>
)
