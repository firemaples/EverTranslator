package tw.firemaples.onscreenocr.translator

object OCROnlyTranslator : Translator {
    override val type: TranslationProviderType
        get() = TranslationProviderType.OCROnly

    override val translationHint: String
        get() = "Translation language is not available for OCR Only mode"

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult =
        TranslationResult.OCROnlyResult
}
