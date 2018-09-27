package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.google.android.gms.common.GoogleApiAvailability
import tw.firemaples.onscreenocr.ocr.OCRLangUtil
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigUtil
import tw.firemaples.onscreenocr.translate.GoogleTranslateUtil
import tw.firemaples.onscreenocr.translate.TranslationService
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

            updatePlayServiceInfo(context)
        }

        private fun updatePlayServiceInfo(context: Context) {
            Crashlytics.setInt("isPlayServiceAvailable",
                    GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context))

            var playServiceVersionName = ""
            var playServiceVersionCode = -1L
            try {
                context.packageManager.getPackageInfo(
                        GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0)
                        .also { info ->
                            playServiceVersionName = info?.versionName ?: "NoFound"
                            playServiceVersionCode =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        info.longVersionCode
                                    } else {
                                        @Suppress("DEPRECATION")
                                        info.versionCode.toLong()
                                    }
                        }

            } catch (e: Throwable) {
                playServiceVersionName = e.message ?: "Unknown exception"
            }
            Crashlytics.setLong("PlayServiceVersionCode", playServiceVersionCode)
            Crashlytics.setString("PlayServiceVersionName", playServiceVersionName)
        }

        @JvmStatic
        fun logTranslationInfo(text: String, _translateToLang: String, service: TranslationService?) {
            val translateFromLang = OCRLangUtil.selectedLangCode
            val textLength = text.length
            val translateToLang = when (service) {
                TranslationService.GoogleTranslatorApp -> "Google Translation APP"
                else -> _translateToLang
            }
            val serviceName = service?.name ?: "From = To"

            val event = CustomEvent(EVENT_TRANSLATE_TEXT)
                    .putCustomAttribute("Text length", textLength)
                    .putCustomAttribute("Translate from", translateFromLang)
                    .putCustomAttribute("Translate to", translateToLang)
                    .putCustomAttribute("From > to", "$translateFromLang > $translateToLang")
                    .putCustomAttribute("System language", Locale.getDefault().language)
                    .putCustomAttribute("Translate service", serviceName)
            when (service) {
                TranslationService.MicrosoftAzure -> {
                    event.putCustomAttribute("Microsoft translation length", textLength)
                    val groupId = RemoteConfigUtil.microsoftTranslationKeyGroupId
                    event.putCustomAttribute("Microsoft translation group id", groupId)
                    event.putCustomAttribute("Microsoft translation group [$groupId] length",
                            textLength)
                }
                TranslationService.Yandex -> {
                    event.putCustomAttribute("Yandex translation length", textLength)
                }
                TranslationService.GoogleTranslatorApp -> {
                    event.putCustomAttribute("Google translation APP length", textLength)
                }
            }
            logEvent(event)
        }

        @JvmStatic
        fun logGoogleTranslateNotFoundWhenResult(context: Context, e: Throwable) {
            val info = GoogleTranslateUtil.getGoogleTranslateInfo()
            updatePlayServiceInfo(context)

            logException(IllegalStateException("Google translate not found or version is too old: $info", e))

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