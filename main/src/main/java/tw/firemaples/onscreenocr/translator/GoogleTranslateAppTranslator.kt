package tw.firemaples.onscreenocr.translator

object GoogleTranslateAppTranslator : Translator {
    override val type: TranslationProviderType
        get() = TranslationProviderType.GoogleTranslateApp

    override val translationHint: String
        get() = "Select the translation language in Google Translate app"

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult =
        TranslationResult.OuterTranslatorLaunched
}
