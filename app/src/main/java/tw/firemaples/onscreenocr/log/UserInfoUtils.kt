package tw.firemaples.onscreenocr.log

import android.content.Context
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.ocr.OCRFileUtil
import tw.firemaples.onscreenocr.ocr.OCRLangUtil
import tw.firemaples.onscreenocr.translate.TranslationUtil
import java.util.*

object UserInfoUtils {
    private val context by lazy { CoreApplication.instance }
    private val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }

    fun setClientInfo() {
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

        updateClientSettings()
        updatePlayServiceInfo()
    }

    fun updateClientSettings() {
        val trainedDataDownloadSite = OCRFileUtil.trainedDataDownloadSite
        Crashlytics.setString("OCR_Site", trainedDataDownloadSite.key)
        Crashlytics.setString("OCR_Site_Url", trainedDataDownloadSite.url)
        Crashlytics.setString("OCR_Lang_Code", OCRLangUtil.selectedLangCode)
        Crashlytics.setString("OCR_Page_Seg_Mode", OCRLangUtil.pageSegmentationMode)
        Crashlytics.setString("Tran_Lang_Code", TranslationUtil.currentTranslationLangCode)

        firebaseAnalytics.setUserProperty("ocr_site", trainedDataDownloadSite.key)
        firebaseAnalytics.setUserProperty("ocr_site_url", trainedDataDownloadSite.url)
        firebaseAnalytics.setUserProperty("ocr_lang_code", OCRLangUtil.selectedLangCode)
        firebaseAnalytics.setUserProperty("ocr_page_seg_mode", OCRLangUtil.pageSegmentationMode)
        firebaseAnalytics.setUserProperty("tran_lang_code", TranslationUtil.currentTranslationLangCode)
    }

    fun updatePlayServiceInfo() {
        val isPlayServiceAvailable = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        Crashlytics.setBool("isPlayServiceAvailable",
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

        Crashlytics.setLong("PlayServiceVersionCode", playServiceVersionCode)
        Crashlytics.setString("PlayServiceVersionName", playServiceVersionName)
        firebaseAnalytics.setUserProperty("PlayServiceVersionCode", playServiceVersionCode.toString())
        firebaseAnalytics.setUserProperty("PlayServiceVersionName", playServiceVersionName)
    }
}