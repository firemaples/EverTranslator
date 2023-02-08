package tw.firemaples.onscreenocr.translator.utils

import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Constants

object BingTranslateUtils : TranslatorUtils() {
    override val packageName: String
        get() = Constants.PACKAGE_NAME_BING_TRANSLATE

    override val targetActivityName: String
        get() = Constants.TARGET_ACTIVITY_BING_TRANSLATE

    override val type: TranslationProviderType
        get() = TranslationProviderType.BingTranslateApp
}
