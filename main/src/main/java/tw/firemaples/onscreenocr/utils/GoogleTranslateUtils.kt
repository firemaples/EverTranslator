package tw.firemaples.onscreenocr.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import java.util.*

object GoogleTranslateUtils {
    private val logger: Logger by lazy { Logger(GoogleTranslateUtils::class) }

    val context: Context by lazy { CoreApplication.instance }

    fun launchGoogleTranslateApp(text: String) {
        if (!checkGoogleTranslateInstalled()) {
            return
        }

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
        } catch (e: Exception) {
            logger.warn("Launch Google Translate app failed", e)
        }
    }

    fun checkGoogleTranslateInstalled(): Boolean {
        if (Utils.isPackageInstalled(Constants.PACKAGE_NAME_GOOGLE_TRANSLATE)) {
            return true
        }

        DialogView(context).apply {
            setTitle(context.getString(R.string.title_error))
            setMessage(context.getString(R.string.error_google_translate_is_not_found))
            setDialogType(DialogView.DialogType.CONFIRM_CANCEL)

            onButtonOkClicked = {
                launchPlayStoreInstallPage()
            }
        }.attachToScreen()

        return false
    }

    fun launchPlayStoreInstallPage() {
        val url = "market://details?id=${Constants.PACKAGE_NAME_GOOGLE_TRANSLATE}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            logger.warn(t = e)
            Utils.openBrowser(url)
            //TODO show error message
        }
    }
}
