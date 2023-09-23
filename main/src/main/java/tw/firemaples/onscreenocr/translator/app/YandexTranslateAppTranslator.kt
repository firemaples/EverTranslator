package tw.firemaples.onscreenocr.translator.app

import tw.firemaples.onscreenocr.translator.BaseAppTranslator
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.utils.TranslatorUtils
import tw.firemaples.onscreenocr.translator.utils.YandexTranslateUtils

object YandexTranslateAppTranslator : BaseAppTranslator() {
    override val type: TranslationProviderType
        get() = TranslationProviderType.BingTranslateApp

    override val translatorUtils: TranslatorUtils
        get() = YandexTranslateUtils
}
