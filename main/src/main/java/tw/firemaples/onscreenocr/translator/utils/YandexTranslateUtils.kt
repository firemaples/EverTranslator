package tw.firemaples.onscreenocr.translator.utils

import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Constants

object YandexTranslateUtils : TranslatorUtils() {
    override val packageName: String
        get() = Constants.PACKAGE_NAME_YANDEX_TRANSLATE

    override val targetActivityName: String
        get() = Constants.TARGET_ACTIVITY_YANDEX_TRANSLATE

    override val type: TranslationProviderType
        get() = TranslationProviderType.YandexTranslateApp
}
