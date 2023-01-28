package tw.firemaples.onscreenocr.translator

import tw.firemaples.onscreenocr.translator.utils.GoogleTranslateUtils
import tw.firemaples.onscreenocr.translator.utils.TranslatorUtils

object GoogleTranslateAppTranslator : BaseAppTranslator() {
    override val type: TranslationProviderType
        get() = TranslationProviderType.GoogleTranslateApp

    override val translatorUtils: TranslatorUtils
        get() = GoogleTranslateUtils
}
