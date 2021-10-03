package tw.firemaples.onscreenocr.utils

import tw.firemaples.onscreenocr.translator.TranslationProviderType

object Constants {
    val DEFAULT_TRANSLATION_PROVIDER = TranslationProviderType.GoogleTranslateApp
    const val DEFAULT_OCR_LANG = "en"
    const val DEFAULT_TRANSLATION_LANG = "en"

    const val PATH_SCREENSHOT: String = "screenshot"
    const val TIMEOUT_EXTRACT_SCREEN = 1000L
}
