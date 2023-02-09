package tw.firemaples.onscreenocr.translator.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Constants
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils

abstract class TranslatorUtils {
    protected val logger: Logger by lazy { Logger(this::class) }

    protected val context: Context by lazy { CoreApplication.instance }

    abstract val packageName: String

    abstract val targetActivityName: String

    abstract val type: TranslationProviderType

    fun launchTranslator(text: String) {
        if (!checkIsInstalled()) {
            return
        }

        launch(context, text)
    }

    protected fun getLaunchIntent(text: String): Intent {
        return Intent().apply {
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK

            // Only few apps support Intent.ACTION_TRANSLATE
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                action = Intent.ACTION_TRANSLATE
//                putExtra(Intent.EXTRA_TEXT, text)
//                if (packageName.isNotBlank())
//                    setPackage(packageName)
//            } else
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                action = Intent.ACTION_PROCESS_TEXT
                putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                if (packageName.isNotBlank())
                    setPackage(packageName)
            } else {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra("key_text_input", text)
                putExtra("key_text_output", "")
                putExtra("key_language_from", "auto")
                putExtra("key_language_to", "")
                putExtra("key_suggest_translation", "")
                putExtra("key_from_floating_window", false)
                component = ComponentName(packageName, targetActivityName)
            }
        }
    }

    open fun launch(context: Context, text: String) {
        val intent = getLaunchIntent(text)

        try {
            context.startActivity(intent)
            FirebaseEvent.logShow3rdPartyTranslator(type.name)
        } catch (e: Exception) {
            logger.warn("Launch ${type.name} failed", e)
            FirebaseEvent.logShow3rdPartyTranslatorFailed(
                name = type.name, info = getPackageInfo(), e,
            )
        }
    }

    private fun getPackageInfo(): String? {
        return Utils.getPackageInfo(Constants.PACKAGE_NAME_GOOGLE_TRANSLATE)?.let {
            val code =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.longVersionCode
                else it.versionCode.toLong()
            val name = it.versionName
            "$name($code)"
        }
    }

    open fun checkIsInstalled(): Boolean {
        if (Utils.isPackageInstalled(packageName)) {
            return true
        }

        DialogView(context).apply {
            setTitle(context.getString(R.string.title_error))
            setMessage(
                context.getString(
                    R.string.error_translator_app_is_not_found, context.getString(type.nameRes)
                )
            )
            setDialogType(DialogView.DialogType.CONFIRM_CANCEL)

            onButtonOkClicked = {
                launchInstallPage()
            }
        }.attachToScreen()

        return false
    }


    private fun launchInstallPage() {
        val url = "market://details?id=$packageName"
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
