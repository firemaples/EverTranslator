package tw.firemaples.onscreenocr.translate

import android.content.Context
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.ocr.OCRLangUtil

object TranslationManager {
    fun checkResource(context: Context, source: String, target: String, callback: (result: Boolean) -> Unit) =
            currentTranslator?.checkResource(context, source, target, callback) ?: callback(true)

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

        val translator: Translator = currentTranslator ?: return

        FirebaseEvent.logStartTranslationText(text, lang, TranslationUtil.currentService)

        translator.translate(text, lang, callback)
    }

    fun cancel() {

    }

    private val currentTranslator: Translator?
        get() = when (TranslationUtil.currentService) {
            TranslationService.MicrosoftAzure -> MicrosoftApiTranslator
            TranslationService.Yandex -> YandexApiTranslator
            TranslationService.GoogleTranslator -> GoogleWebViewTranslator
            TranslationService.GoogleMLKit -> GoogleMLKitTranslator
            else -> null
        }
}
