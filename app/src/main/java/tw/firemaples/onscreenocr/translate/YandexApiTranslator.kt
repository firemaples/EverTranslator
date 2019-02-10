package tw.firemaples.onscreenocr.translate

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.utils.KeyId
import tw.firemaples.onscreenocr.utils.threadTranslation
import java.io.IOException

object YandexApiTranslator : Translator {
    private val logger: Logger = LoggerFactory.getLogger(YandexApiTranslator::class.java)
    private const val CODE_RESULT_OK = 200

    override fun translate(text: String, lang: String,
                           callback: (Boolean, String, Throwable?) -> Unit
    ) = GlobalScope.launch(threadTranslation) {
        val key = KeyId.YANDEX_TRANSLATE_KEY

        val url = "https://translate.yandex.net/api/v1.5/tr.json/translate?" +
                "key=$key&text=$text&lang=$lang"

        logger.info("Start Yandex translation: $url")

        val request = AndroidNetworking.get(url).setPriority(Priority.HIGH).build()
        request.getAsObject(TranslateResult::class.java, object : ParsedRequestListener<TranslateResult> {
            override fun onResponse(response: TranslateResult?) {
                val errorReason: String = if (response != null) {
                    logger.info("Response from Yandex: $response")
                    if (response.code == CODE_RESULT_OK) {
                        if (response.text?.get(0)?.isNotBlank() == true) {
                            callback(true, response.text?.get(0) ?: "", null)
                            return
                        } else {
                            "Result is blank, response: $response"
                        }
                    } else {
                        "Yandex result is not ok, response: $response"
                    }
                } else {
                    "Yandex response null object"
                }

                val msg = "Parsing Yandex translation result failed, $errorReason"
                logger.error(msg)

                callback(false, "", IllegalArgumentException(msg))
            }

            override fun onError(anError: ANError?) {
                logger.error("onError", anError)

                callback(false, "", anError)
            }

        })
    }

}

data class TranslateResult(
        var code: Int,
        var lang: String?,
        @Suppress("ArrayInDataClass") var text: Array<String>?)