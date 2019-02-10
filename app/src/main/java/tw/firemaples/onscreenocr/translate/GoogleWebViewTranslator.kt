package tw.firemaples.onscreenocr.translate

import com.firemaples.googlewebtranslator.GoogleWebTranslator
import com.firemaples.googlewebtranslator.TranslatedResult
import kotlinx.coroutines.Job
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.utils.launch
import tw.firemaples.onscreenocr.utils.threadTranslation
import java.io.IOException

object GoogleWebViewTranslator : Translator {
    val translator: GoogleWebTranslator by lazy {
        GoogleWebTranslator(CoreApplication.instance)
    }

    override fun translate(text: String, lang: String, callback: (Boolean, String, Throwable?) -> Unit): Job = threadTranslation.launch {
        translator.translate(text, lang, object : GoogleWebTranslator.OnTranslationCallback {
            override fun onStart() {

            }

            override fun onTranslated(result: TranslatedResult) {
                callback(true, result.text, null)
            }

            override fun onTranslationFailed(errorMsg: String) {
                callback(false, "", IOException(errorMsg))
            }
        })
    }
}