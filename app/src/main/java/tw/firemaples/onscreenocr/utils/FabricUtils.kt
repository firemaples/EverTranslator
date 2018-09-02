package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import java.util.*

class FabricUtils {
    companion object {
        @JvmStatic
        fun setClientInfo(context: Context) {
            val locale = Locale.getDefault()
            Crashlytics.setString("DeviceLanguage", locale.language)
            Crashlytics.setString("DisplayLanguage", locale.displayLanguage)
            val config = context.resources.configuration
            val configLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.locales[0]
            } else {
                @Suppress("DEPRECATION")
                config.locale
            }
            Crashlytics.setString("CountryCode", configLocale.country)
            Crashlytics.setString("DisplayCountry", configLocale.displayCountry)
        }

        @JvmStatic
        fun logTranslationInfo(text: String, translateFromLang: String, translateToLang: String, serviceName: String) {
            Answers.getInstance().logCustom(
                    CustomEvent("Translate Text")
                            .putCustomAttribute("Text length", text.length)
                            .putCustomAttribute("Translate from", translateFromLang)
                            .putCustomAttribute("Translate to", translateToLang)
                            .putCustomAttribute("From > to", "$translateFromLang > $translateToLang")
                            .putCustomAttribute("System language", Locale.getDefault().language)
                            .putCustomAttribute("Translate service", serviceName)
            )
        }

        @JvmStatic
        fun logException(t: Throwable) {
            Crashlytics.logException(t)
        }
    }
}