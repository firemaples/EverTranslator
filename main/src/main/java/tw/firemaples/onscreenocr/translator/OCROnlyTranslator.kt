package tw.firemaples.onscreenocr.translator

import tw.firemaples.onscreenocr.R

object OCROnlyTranslator : Translator {
    override val type: TranslationProviderType
        get() = TranslationProviderType.OCROnly

    override val translationHint: String
        get() = context.getString(R.string.msg_ocr_only_mode_hint)

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult =
        TranslationResult.OCROnlyResult
}
