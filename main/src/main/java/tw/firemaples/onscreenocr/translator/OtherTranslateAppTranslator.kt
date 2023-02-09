package tw.firemaples.onscreenocr.translator

import tw.firemaples.onscreenocr.translator.utils.OtherTranslateUtils
import tw.firemaples.onscreenocr.translator.utils.TranslatorUtils

object OtherTranslateAppTranslator : BaseAppTranslator() {
    override val type: TranslationProviderType
        get() = TranslationProviderType.OtherTranslateApp

    override val translatorUtils: TranslatorUtils
        get() = OtherTranslateUtils
}
