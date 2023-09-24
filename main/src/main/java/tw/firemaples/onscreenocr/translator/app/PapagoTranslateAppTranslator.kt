package tw.firemaples.onscreenocr.translator.app

import tw.firemaples.onscreenocr.translator.BaseAppTranslator
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.utils.PapagoTranslateUtils
import tw.firemaples.onscreenocr.translator.utils.TranslatorUtils

object PapagoTranslateAppTranslator: BaseAppTranslator() {
    override val type: TranslationProviderType
        get() = TranslationProviderType.PapagoTranslateApp

    override val translatorUtils: TranslatorUtils
        get() = PapagoTranslateUtils
}
