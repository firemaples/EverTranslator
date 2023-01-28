package tw.firemaples.onscreenocr.utils

import tw.firemaples.onscreenocr.floatings.textInfoSearch.TextInfoSearchViewModel
import tw.firemaples.onscreenocr.recognition.TextRecognitionProviderType
import tw.firemaples.onscreenocr.translator.TranslationProviderType

object Constants {
    val DEFAULT_TRANSLATION_PROVIDER = TranslationProviderType.GoogleMLKit
    val DEFAULT_OCR_PROVIDER = TextRecognitionProviderType.GoogleMLKit
    const val DEFAULT_OCR_LANG = "en"
    const val DEFAULT_TRANSLATION_LANG = "en"

    const val PATH_SCREENSHOT: String = "screenshot"
    const val TIMEOUT_EXTRACT_SCREEN = 5

    const val MIN_SCREEN_CROP_SIZE = 32

    const val PACKAGE_NAME_GOOGLE_TRANSLATE = "com.google.android.apps.translate"
    const val PACKAGE_NAME_PAPAGO_TRANSLATE = "com.naver.labs.translator"
    const val PACKAGE_NAME_BING_TRANSLATE = "com.microsoft.translator"
    const val PACKAGE_NAME_YANDEX_TRANSLATE = "ru.yandex.translate"

    val regexForImageReaderFormatError: Regex by lazy {
        "The producer output buffer format 0x(\\d+) doesn't match the ImageReader's configured buffer format 0x\\d+".toRegex()
    }

    const val errorInputImageIsTooSmall: String = "InputImage width and height should be at least"

    const val DEFAULT_RESULT_WINDOW_FONT_SIZE = 14f

    val DEFAULT_TEXT_INFO_SEARCH_PAGE = TextInfoSearchViewModel.PageType.GoogleTranslate
}
