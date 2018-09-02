package tw.firemaples.onscreenocr.translate

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import kotlinx.coroutines.experimental.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.utils.KeyId
import tw.firemaples.onscreenocr.utils.threadTranslation

object YandexApiTranslator : Translator {
    private val logger: Logger = LoggerFactory.getLogger(YandexApiTranslator::class.java)
    private const val CODE_RESULT_OK = 200

    override fun translate(text: String, lang: String,
                           callback: (Boolean, String, Throwable?) -> Unit
    ) = launch(threadTranslation) {
        val key = KeyId.YANDEX_TRANSLATE_KEY

        val url = "https://translate.yandex.net/api/v1.5/tr.json/translate?" +
                "key=$key&text=$text&lang=$lang"

        logger.info("Start Yandex translation: $url")

        val request = AndroidNetworking.get(url).setPriority(Priority.HIGH).build()
        request.getAsObject(TranslateResult::class.java, object : ParsedRequestListener<TranslateResult> {
            override fun onResponse(response: TranslateResult?) {
                if (response != null) {
                    logger.info("Response from Yandex: $response")
                    if (response.code == CODE_RESULT_OK) {
                        if (response.text?.get(0)?.isNotBlank() == true) {
                            callback(true, response.text?.get(0) ?: "", null)
                            return
                        } else {
                            logger.info("Result is blank")
                        }
                    } else {
                        logger.info("Yandex result is not ok")
                    }
                } else {
                    logger.info("Yandex response null object")
                }
                callback(false, "", IllegalStateException("Get Yandex translation result failed"))
            }

            override fun onError(anError: ANError?) {
                logger.error("onError", anError)
                callback(false, "", IllegalStateException("Get Yandex translation result failed"))
            }

        })
    }

}

data class TranslateResult(
        var code: Int,
        var lang: String?,
        @Suppress("ArrayInDataClass") var text: Array<String>?)