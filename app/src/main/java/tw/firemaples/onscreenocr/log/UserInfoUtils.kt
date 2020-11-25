package tw.firemaples.onscreenocr.log

import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.ocr.OCRFileUtil
import tw.firemaples.onscreenocr.ocr.OCRLangUtil
import tw.firemaples.onscreenocr.translate.TranslationUtil
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

    fun updateClientSettings() {
        val trainedDataDownloadSite = OCRFileUtil.trainedDataDownloadSite
        firebaseCrashlytics.setCustomKey("OCR_Site", trainedDataDownloadSite.key)
        firebaseCrashlytics.setCustomKey("OCR_Site_Url", trainedDataDownloadSite.url)
        firebaseCrashlytics.setCustomKey("OCR_Lang_Code", OCRLangUtil.selectedLangCode)
        firebaseCrashlytics.setCustomKey("OCR_Page_Seg_Mode", OCRLangUtil.pageSegmentationMode)
        firebaseCrashlytics.setCustomKey("Tran_Lang_Code", TranslationUtil.currentTranslationLangCode)

        firebaseAnalytics.setUserProperty("ocr_site", trainedDataDownloadSite.key)
        firebaseAnalytics.setUserProperty("ocr_site_url", trainedDataDownloadSite.url)
        firebaseAnalytics.setUserProperty("ocr_lang_code", OCRLangUtil.selectedLangCode)
        firebaseAnalytics.setUserProperty("ocr_page_seg_mode", OCRLangUtil.pageSegmentationMode)
        firebaseAnalytics.setUserProperty("tran_lang_code", TranslationUtil.currentTranslationLangCode)
    }

    fun updatePlayServiceInfo() {
        val isPlayServiceAvailable = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        firebaseCrashlytics.setCustomKey("isPlayServiceAvailable",
                isPlayServiceAvailable)
        firebaseAnalytics.setUserProperty("play_service_available", isPlayServiceAvailable.toString())

        var playServiceVersionName: String
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

        firebaseCrashlytics.setCustomKey("PlayServiceVersionCode", playServiceVersionCode)
        firebaseCrashlytics.setCustomKey("PlayServiceVersionName", playServiceVersionName)
        firebaseAnalytics.setUserProperty("PlayServiceVersionCode", playServiceVersionCode.toString())
        firebaseAnalytics.setUserProperty("PlayServiceVersionName", playServiceVersionName)
    }
}