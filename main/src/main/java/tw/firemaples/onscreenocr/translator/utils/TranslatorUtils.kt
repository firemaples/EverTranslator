package tw.firemaples.onscreenocr.translator.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils

abstract class TranslatorUtils {
    protected val logger: Logger by lazy { Logger(this::class) }

    private val context: Context by lazy { CoreApplication.instance }

    abstract val packageName: String

    abstract val type: TranslationProviderType

    fun launchTranslator(text: String) {
        if (!checkIsInstalled()) {
            return
        }

        launch(context, text)
    }

    abstract fun launch(context: Context, text: String)

    fun checkIsInstalled(): Boolean {
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
