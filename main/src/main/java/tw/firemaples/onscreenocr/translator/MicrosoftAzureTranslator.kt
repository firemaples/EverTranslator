package tw.firemaples.onscreenocr.translator

import tw.firemaples.onscreenocr.R

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

    override suspend fun translate(text: String): String {
        TODO("Not yet implemented")
    }
}
