package tw.firemaples.onscreenocr.translator

import tw.firemaples.onscreenocr.translator.utils.TranslatorUtils
import tw.firemaples.onscreenocr.translator.utils.YandexTranslateUtils

object YandexTranslateAppTranslator : BaseAppTranslator() {
    override val type: TranslationProviderType
        get() = TranslationProviderType.BingTranslateApp

    override val translatorUtils: TranslatorUtils
        get() = YandexTranslateUtils
}
