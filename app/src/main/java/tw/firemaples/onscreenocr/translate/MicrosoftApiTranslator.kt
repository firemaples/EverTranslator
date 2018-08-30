package tw.firemaples.onscreenocr.translate

import io.github.firemaples.language.Language
import io.github.firemaples.translate.Translate
import kotlinx.coroutines.experimental.launch
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigUtil
import tw.firemaples.onscreenocr.utils.threadTranslation
import tw.firemaples.onscreenocr.utils.threadUI

object MicrosoftApiTranslator : Translator {
    override fun translate(text: String, lang: String,
                           callback: (Boolean, String, Throwable?) -> Unit
    ) = launch(threadTranslation) {
        Translate.setSubscriptionKey(RemoteConfigUtil.microsoftTranslationKey)
        Translate.setUsingSSL(true)
        try {
            val result = Translate.execute(text, Language.fromString(lang))
            threadUI {
                callback(true, result, null)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            threadUI {
                callback(false, "", e)
            }
        }

    }
}