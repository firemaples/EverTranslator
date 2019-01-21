package tw.firemaples.onscreenocr.translate

import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.ocr.OCRLangUtil

object TranslationManager {
    fun startTranslation(text: String, lang: String, callback: (Boolean, String, Throwable?) -> Unit) {
        if (text.isBlank() || !TranslationUtil.isEnableTranslation) {
            callback(true, "", null)
            return
        }

        if (lang == OCRLangUtil.selectLangDisplayCode) {
            FirebaseEvent.logStartTranslationText(text, lang, null)
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

        FirebaseEvent.logStartTranslationText(text, lang, TranslationUtil.currentService)

        translator.translate(text, lang, callback)
    }

    fun cancel() {

    }
}