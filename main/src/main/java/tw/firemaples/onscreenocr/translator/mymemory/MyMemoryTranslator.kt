package tw.firemaples.onscreenocr.translator.mymemory

import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.translator.TranslationLanguage
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.TranslationResult
import tw.firemaples.onscreenocr.translator.Translator
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.firstPart

object MyMemoryTranslator : Translator {
    private val logger: Logger by lazy { Logger(this::class) }

    override val type: TranslationProviderType
        get() = TranslationProviderType.MyMemory

    override val defaultLanguage: String
        get() = "en-GB"

    override suspend fun supportedLanguages(): List<TranslationLanguage> {
        val langCodeList =
            context.resources.getStringArray(R.array.mymemory_translationLangCode_iso639_iso3166)
        val langNameList = context.resources.getStringArray(R.array.mymemory_translationLangName)

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
        if (!isLangSupport()) {
            return TranslationResult.SourceLangNotSupport(type)
        }

        if (text.isBlank()) {
            return TranslationResult.TranslatedResult(result = "", type)
        }

        val targetLangCode = supportedLanguages().firstOrNull { it.selected }?.code
            ?: return TranslationResult.TranslationFailed(IllegalArgumentException("The selected translation language is not found"))

        if (AppPref.selectedOCRLang.firstPart() == targetLangCode) {
            return TranslationResult.TranslatedResult(result = text, type)
        }

        return doTranslate(
            text = text,
            sourceLangCode = sourceLangCode,
            targetLangCode = targetLangCode,
        )
    }

    private suspend fun doTranslate(
        text: String,
        sourceLangCode: String,
        targetLangCode: String,
    ): TranslationResult {
        MyMemoryTranslatorAPI.translate(
            text = text,
            from = sourceLangCode,
            to = targetLangCode,
            email = null,
        ).onSuccess {
            return TranslationResult.TranslatedResult(it, type)
        }.onFailure {
            return TranslationResult.TranslationFailed(it)
        }

        return TranslationResult.TranslationFailed(IllegalStateException("Illegal state"))
    }
}
