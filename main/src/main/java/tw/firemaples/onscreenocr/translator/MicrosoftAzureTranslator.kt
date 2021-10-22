package tw.firemaples.onscreenocr.translator

import io.github.firemaples.language.Language
import io.github.firemaples.translate.Translate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager

object MicrosoftAzureTranslator : Translator {
    override val type: TranslationProviderType
        get() = TranslationProviderType.MicrosoftAzure

    override suspend fun supportedLanguages(): List<TranslationLanguage> {
        val langCodeList =
            context.resources.getStringArray(R.array.microsoft_translationLangCode_iso6391)
        val langNameList = context.resources.getStringArray(R.array.microsoft_translationLangName)

        val selectedLangCode = selectedLangCode(langCodeList)

        return (langCodeList.indices).map { i ->
            val code = langCodeList[i]
            val name = langNameList[i]

            TranslationLanguage(
                code = code,
                displayName = name,
                selected = code == selectedLangCode
            )
        }
    }

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult {
        if (text.isBlank()) {
            return TranslationResult.TranslatedResult(result = "", type)
        }

        val targetLang = supportedLanguages().firstOrNull { it.selected }?.code
            ?.let { Language.fromString(it) }

        Translate.setSubscriptionKey(RemoteConfigManager.microsoftTranslationKey)

        return try {
            val result = withContext(Dispatchers.IO) {
                Translate.execute(text, targetLang)
            }

            TranslationResult.TranslatedResult(result, type)
        } catch (e: Exception) {
            TranslationResult.TranslationFailed(e)
        }
    }
}
