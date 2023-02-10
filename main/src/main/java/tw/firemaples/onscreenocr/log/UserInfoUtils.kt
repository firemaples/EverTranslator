package tw.firemaples.onscreenocr.log

import android.os.Build
import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import java.util.*

object UserInfoUtils {
    private val context by lazy { CoreApplication.instance }
    private val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }
    private val firebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }

    fun setClientInfo() {
        val locale = Locale.getDefault()
        firebaseCrashlytics.setCustomKey("DeviceLanguage", locale.language)
        firebaseCrashlytics.setCustomKey("DisplayLanguage", locale.displayLanguage)
        val config = context.resources.configuration
        val configLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0]
        } else {
            @Suppress("DEPRECATION")
            config.locale
        }
        firebaseCrashlytics.setCustomKey("CountryCode", configLocale.country)
        firebaseCrashlytics.setCustomKey("DisplayCountry", configLocale.displayCountry)

        updateClientSettings()
        updatePlayServiceInfo()
    }

    private fun updateClientSettings() {
        CoroutineScope(Dispatchers.Default).launch {
            AppPref.asLiveData(AppPref::selectedOCRLang).asFlow().collect {
                firebaseAnalytics.setUserProperty("ocr_lang_code", it)
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            AppPref.asLiveData(AppPref::selectedTranslationLang).asFlow().collect {
                firebaseAnalytics.setUserProperty("tran_lang_code", it)
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            AppPref.asLiveData(AppPref::selectedTranslationProvider).asFlow().collect {
                firebaseAnalytics.setUserProperty("current_translate_svc", it)
            }
        }

        firebaseAnalytics.setUserProperty(
            "ms_translate_key_group",
            RemoteConfigManager.microsoftTranslationKeyGroupId
        )
    }

    fun updatePlayServiceInfo() {
        val isPlayServiceAvailable = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        firebaseCrashlytics.setCustomKey(
            "isPlayServiceAvailable",
            isPlayServiceAvailable
        )
        firebaseAnalytics.setUserProperty(
            "play_service_available",
            isPlayServiceAvailable.toString()
        )

        var playServiceVersionName: String
        var playServiceVersionCode = -1L
        try {
            context.packageManager.getPackageInfo(
                GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0
            )
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

        firebaseCrashlytics.setCustomKey("PlayServiceVersionCode", playServiceVersionCode)
        firebaseCrashlytics.setCustomKey("PlayServiceVersionName", playServiceVersionName)
        firebaseAnalytics.setUserProperty("play_service_vs_code", playServiceVersionCode.toString())
        firebaseAnalytics.setUserProperty("play_service_vs_name", playServiceVersionName)
    }
}
