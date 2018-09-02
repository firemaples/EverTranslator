package tw.firemaples.onscreenocr.translate

import tw.firemaples.onscreenocr.utils.SettingUtil

object TranslationManager {
    fun startTranslation(text: String, lang: String, callback: (Boolean, String, Throwable?) -> Unit) {
        if (text.isBlank() || !SettingUtil.enableTranslation) {
            callback(true, "", null)
            return
        }

        //TODO Check from & to is same

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

        translator.translate(text, lang, callback)
    }

    fun cancel() {

    }
}