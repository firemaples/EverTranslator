package tw.firemaples.onscreenocr.translator.app

import tw.firemaples.onscreenocr.translator.BaseAppTranslator
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.utils.BingTranslateUtils
import tw.firemaples.onscreenocr.translator.utils.TranslatorUtils

object BingTranslateAppTranslator : BaseAppTranslator() {
    override val type: TranslationProviderType
        get() = TranslationProviderType.BingTranslateApp

    override val translatorUtils: TranslatorUtils
        get() = BingTranslateUtils
}
