package tw.firemaples.onscreenocr.translator.utils

import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Constants

object PapagoTranslateUtils : TranslatorUtils() {
    override val packageName: String
        get() = Constants.PACKAGE_NAME_PAPAGO_TRANSLATE

    override val targetActivityName: String
        get() = Constants.TARGET_ACTIVITY_PAPAGO_TRANSLATE

    override val type: TranslationProviderType
        get() = TranslationProviderType.PapagoTranslateApp
}
