package tw.firemaples.onscreenocr.utils

import tw.firemaples.onscreenocr.translator.TranslationProviderType

object Constants {
    val DEFAULT_TRANSLATION_PROVIDER = TranslationProviderType.GoogleMLKit
    const val DEFAULT_OCR_LANG = "en"
    const val DEFAULT_TRANSLATION_LANG = "en"

    const val PATH_SCREENSHOT: String = "screenshot"
    const val TIMEOUT_EXTRACT_SCREEN = 5

    const val PACKAGE_NAME_GOOGLE_TRANSLATE = "com.google.android.apps.translate"

    val regexForImageReaderFormatError: Regex by lazy {
        "The producer output buffer format 0x(\\d+) doesn't match the ImageReader's configured buffer format 0x\\d+".toRegex()
    }

    const val errorInputImageIsTooSmall: String = "InputImage width and height should be at least"
}
