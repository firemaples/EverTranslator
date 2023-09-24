package tw.firemaples.onscreenocr.translator.ocronly

import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.TranslationResult
import tw.firemaples.onscreenocr.translator.Translator

object OCROnlyTranslator : Translator {
    override val type: TranslationProviderType
        get() = TranslationProviderType.OCROnly

    override val translationHint: String
        get() = context.getString(R.string.msg_ocr_only_mode_hint)

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult =
        TranslationResult.OCROnlyResult
}
