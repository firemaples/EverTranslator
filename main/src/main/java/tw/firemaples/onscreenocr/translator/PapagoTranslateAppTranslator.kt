package tw.firemaples.onscreenocr.translator

import tw.firemaples.onscreenocr.translator.utils.PapagoTranslateUtils
import tw.firemaples.onscreenocr.translator.utils.TranslatorUtils

object PapagoTranslateAppTranslator: BaseAppTranslator() {
    override val type: TranslationProviderType
        get() = TranslationProviderType.PapagoTranslateApp

    override val translatorUtils: TranslatorUtils
        get() = PapagoTranslateUtils
}
