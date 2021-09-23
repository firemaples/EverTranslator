package tw.firemaples.onscreenocr.translator

import android.content.Context
import androidx.annotation.StringRes
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.utils.Constraints
import tw.firemaples.onscreenocr.utils.Utils

interface Translator {
    companion object {
        fun getTranslator(type: TranslationProviderType): Translator =
            when (type) {
                TranslationProviderType.MicrosoftAzure -> MicrosoftAzureTranslator
                TranslationProviderType.GoogleMLKit -> GoogleMLKitTranslator
                TranslationProviderType.GoogleTranslateApp -> GoogleTranslateAppTranslator
                TranslationProviderType.OCROnly -> OCROnlyTranslator
            }
    }

    val type: TranslationProviderType
    val context: Context
        get() = Utils.context
    open val translationHint: String?
        get() = null

    suspend fun supportedLanguages(): List<TranslationLanguage> = emptyList()
    suspend fun translate(text: String): String
    suspend fun selectedLangCode(supportedLangList: Array<String>): String {
        val selectedLangCode = AppPref.selectedTranslationLang

        return if (supportedLangList.any { it == selectedLangCode }) selectedLangCode
        else {
            AppPref.selectedTranslationLang = Constraints.DEFAULT_TRANSLATION_LANG
            Constraints.DEFAULT_TRANSLATION_LANG
        }
    }
}

enum class TranslationProviderType(
    val index: Int,
    val key: String,
    @StringRes val nameRes: Int,
    val nonTranslation: Boolean = false
) {
    MicrosoftAzure(0, "microsoft_azure", R.string.translation_provider_microsoft_azure),
    GoogleMLKit(1, "google_ml_kit", R.string.translation_provider_google_ml_kit),
    GoogleTranslateApp(
        2, "google_translate_app", R.string.translation_provider_google_translate_app,
        nonTranslation = true
    ),
    OCROnly(3, "ocr_only", R.string.translation_provider_none, nonTranslation = true);

    companion object {
        fun fromKey(key: String): TranslationProviderType =
            values().firstOrNull { it.key == key } ?: Constraints.DEFAULT_TRANSLATION_PROVIDER
    }
}

data class TranslationProvider(
    val key: String,
    val displayName: String,
    val nonTranslation: Boolean,
    val type: TranslationProviderType,
    val selected: Boolean,
) {
    companion object {
        fun fromType(
            context: Context, type: TranslationProviderType, selected: Boolean = false
        ): TranslationProvider =
            TranslationProvider(
                key = type.key,
                displayName = context.getString(type.nameRes),
                nonTranslation = type.nonTranslation,
                type = type,
                selected = selected,
            )
    }
}

data class TranslationLanguage(
    val code: String, /*val langCode: String,*/
    val displayName: String,
    val selected: Boolean
)
