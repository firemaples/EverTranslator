package tw.firemaples.onscreenocr.translate

import tw.firemaples.onscreenocr.ocr.OCRLangUtil
import tw.firemaples.onscreenocr.utils.FabricUtils
import tw.firemaples.onscreenocr.utils.SettingUtil

object TranslationManager {
    fun startTranslation(text: String, lang: String, callback: (Boolean, String, Throwable?) -> Unit) {
        if (text.isBlank() || !SettingUtil.enableTranslation) {
            callback(true, "", null)
            return
        }

        if (lang == OCRLangUtil.selectLangDisplayCode) {
            FabricUtils.logTranslationInfo(text, lang, null)
            callback(true, text, null)
            return
        }

        val translator: Translator = when (TranslationUtil.currentService) {
            TranslationService.MicrosoftAzure -> {
                MicrosoftApiTranslator
            }
            TranslationService.Yandex -> {
                YandexApiTranslator
            }
            else -> {
                return
            }
        }

        FabricUtils.logTranslationInfo(text, lang, TranslationUtil.currentService)

        translator.translate(text, lang, callback)
    }

    fun cancel() {

    }
}