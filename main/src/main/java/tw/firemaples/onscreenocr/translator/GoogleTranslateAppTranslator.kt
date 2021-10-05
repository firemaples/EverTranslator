package tw.firemaples.onscreenocr.translator

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.utils.Constants
import tw.firemaples.onscreenocr.utils.Utils
import java.util.*

object GoogleTranslateAppTranslator : Translator {
    override val type: TranslationProviderType
        get() = TranslationProviderType.GoogleTranslateApp

    override val translationHint: String
        get() = "Select the translation language in Google Translate app"

    override suspend fun checkEnvironment(
        coroutineScope: CoroutineScope
    ): Boolean = checkGoogleTranslateInstalled()

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult {
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

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            //TODO Google translate app not found
        }

        return TranslationResult.OuterTranslatorLaunched
    }

    private fun checkGoogleTranslateInstalled(
    ): Boolean {
        if (Utils.isPackageInstalled(Constants.PACKAGE_NAME_GOOGLE_TRANSLATE)) {
            return true
        }

        DialogView(context).apply {
            setTitle("Error")
            setMessage("The Google Translate app is not found on your device, the translation provider is dependence on the app, please click the OK button to install it on PlayStore or change to another translation provider for translation.")
            setDialogType(DialogView.DialogType.CONFIRM_CANCEL)

            onButtonOkClicked = {
                launchPlayStore()
            }
        }.attachToScreen()

        return false
    }

    private fun launchPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=${Constants.PACKAGE_NAME_GOOGLE_TRANSLATE}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            //TODO show error dialog
        }
    }
}
