package tw.firemaples.onscreenocr.translator.app

import tw.firemaples.onscreenocr.translator.BaseAppTranslator
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.utils.GoogleTranslateUtils
import tw.firemaples.onscreenocr.translator.utils.TranslatorUtils

object GoogleTranslateAppTranslator : BaseAppTranslator() {
    override val type: TranslationProviderType
        get() = TranslationProviderType.GoogleTranslateApp

    override val translatorUtils: TranslatorUtils
        get() = GoogleTranslateUtils
}
