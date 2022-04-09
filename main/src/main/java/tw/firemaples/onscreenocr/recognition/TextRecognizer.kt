package tw.firemaples.onscreenocr.recognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.annotation.StringRes
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.pages.setting.SettingManager
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
        private val recognizerCache = mutableMapOf<TextRecognitionProviderType, TextRecognizer>()
        private val enableAllTesseractLangItems: Boolean
            get() = SettingManager.enableUnrecommendedLangItems

        fun getRecognizer(recognizer: TextRecognitionProviderType): TextRecognizer {
            return recognizerCache.getOrPut(recognizer) {
                when (recognizer) {
                    TextRecognitionProviderType.GoogleMLKit -> GoogleMLKitTextRecognizer()
                    TextRecognitionProviderType.Tesseract -> TesseractTextRecognizer()
                }
            }
        }

        private fun initializeSupportLanguages(): List<RecognitionLanguage> {
            val mlKitLangList = GoogleMLKitTextRecognizer.getSupportedLanguageList(context)
            val tesseractLangList = TesseractTextRecognizer.getSupportedLanguageList(context)

            val langList = mlKitLangList.toMutableList()
            val exists = langList.map { it.code }.toSet()

            langList += tesseractLangList.mapNotNull {
                if (exists.contains(it.code)) {
                    if (enableAllTesseractLangItems) it.apply { unrecommended = true }
                    else null
                } else it
            }

            return langList.sortedBy { it.displayName }
        }

        fun allSupportedLanguages(
            selectedLang: String,
            providerType: TextRecognitionProviderType,
        ): List<RecognitionLanguage> {
            val selected = fixSelectLang(selectedLang)

            return supportedLanguages.map {
                when {
                    it.code == selected && it.recognizer == providerType ->
                        if (it.selected) it else it.copy(selected = true)
                    it.selected -> it.copy(selected = false)
                    else -> it
                }
            }
        }

        fun invalidSupportLanguages() {
            supportedLanguagesCache = listOf()
        }

        fun getLanguage(
            lang: String,
            recognizer: TextRecognitionProviderType
        ): RecognitionLanguage? =
            supportedLanguages.firstOrNull { it.code == lang && it.recognizer == recognizer }

        private fun fixSelectLang(selectedLang: String): String =
            if (supportedLanguages.any { it.code == selectedLang }) selectedLang
            else Constants.DEFAULT_OCR_LANG
    }

    val type: TextRecognitionProviderType
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

data class RecognitionLanguage(
    val code: String,
    val displayName: String,
    val selected: Boolean,
    val downloaded: Boolean,
    val recognizer: TextRecognitionProviderType,
    val innerCode: String,
    var unrecommended: Boolean = false,
)

data class RecognitionResult(
    val langCode: String,
    val result: String,
    val boundingBoxes: List<Rect>
)
