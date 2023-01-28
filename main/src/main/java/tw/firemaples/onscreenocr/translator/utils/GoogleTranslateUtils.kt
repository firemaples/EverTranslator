package tw.firemaples.onscreenocr.translator.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Constants
import tw.firemaples.onscreenocr.utils.Utils
import java.util.*

object GoogleTranslateUtils : TranslatorUtils() {

    override val packageName: String
        get() = Constants.PACKAGE_NAME_GOOGLE_TRANSLATE

    override val type: TranslationProviderType
        get() = TranslationProviderType.GoogleTranslateApp

    override fun launch(context: Context, text: String) {
        val langTo = Locale.getDefault().language

        val intent = Intent().apply {
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                action = Intent.ACTION_PROCESS_TEXT
                putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                putExtra("key_language_to", langTo)
                setPackage(Constants.PACKAGE_NAME_GOOGLE_TRANSLATE)
            } else {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra("key_text_input", text)
                putExtra("key_text_output", "")
                putExtra("key_language_from", "auto")
                putExtra("key_language_to", langTo)
                putExtra("key_suggest_translation", "")
                putExtra("key_from_floating_window", false)
                component = ComponentName(
                    Constants.PACKAGE_NAME_GOOGLE_TRANSLATE,
                    //Change is here
                    //"com.google.android.apps.translate.HomeActivity"));
                    "com.google.android.apps.translate.TranslateActivity"
                )
            }
        }

        intent.resolveActivity(context.packageManager)
        try {
            context.startActivity(intent)
            FirebaseEvent.logShowGoogleTranslateWindow()
        } catch (e: Exception) {
            logger.warn("Launch Google Translate app failed", e)
            FirebaseEvent.logShowGoogleTranslateWindowFailed(e)
        }
    }

    fun getGoogleTranslateInfo(): GoogleTranslateInfo? =
        Utils.getPackageInfo(Constants.PACKAGE_NAME_GOOGLE_TRANSLATE)?.let {
            @Suppress("DEPRECATION")
            (GoogleTranslateInfo(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    it.longVersionCode
                else
                    it.versionCode.toLong(),
                it.versionName
            ))
        }

    data class GoogleTranslateInfo(val versionCode: Long, val versionName: String)
}
