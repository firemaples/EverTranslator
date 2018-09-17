package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import tw.firemaples.onscreenocr.translate.GoogleTranslateUtil
import java.util.*

private const val EVENT_TRANSLATE_TEXT = "Translate Text"
private const val EVENT_GOOGLE_TRANSLATE_NOT_FOUND = "Google Translate not found"

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
                    CustomEvent(EVENT_TRANSLATE_TEXT)
                            .putCustomAttribute("Text length", text.length)
                            .putCustomAttribute("Translate from", translateFromLang)
                            .putCustomAttribute("Translate to", translateToLang)
                            .putCustomAttribute("From > to", "$translateFromLang > $translateToLang")
                            .putCustomAttribute("System language", Locale.getDefault().language)
                            .putCustomAttribute("Translate service", serviceName)
            )
        }

        @JvmStatic
        fun logGoogleTranslateNotFoundWhenResult() {
            val info = GoogleTranslateUtil.getGoogleTranslateInfo()
            logException(IllegalStateException("Google translate not found or version is too old: $info"))
            logEvent(CustomEvent(EVENT_GOOGLE_TRANSLATE_NOT_FOUND)
                    .putCustomAttribute("PackageInfoExists", (info != null).toString())
                    .putCustomAttribute("VersionCode", info?.versionCode ?: -1)
                    .putCustomAttribute("VersionName", info?.versionName ?: "Not found"))
        }

        private fun logEvent(event: CustomEvent) {
            Answers.getInstance().logCustom(event)
        }

        @JvmStatic
        fun logException(t: Throwable) {
            Crashlytics.logException(t)
        }
    }
}