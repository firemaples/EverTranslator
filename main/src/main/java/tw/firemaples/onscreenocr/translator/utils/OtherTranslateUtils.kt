package tw.firemaples.onscreenocr.translator.utils

import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.translator.TranslationProviderType

object OtherTranslateUtils : TranslatorUtils() {
    override val packageName: String
        get() = ""

    override val targetActivityName: String
        get() = ""

    override val type: TranslationProviderType
        get() = TranslationProviderType.OtherTranslateApp

    override fun checkIsInstalled(): Boolean {
        val intent = getLaunchIntent("")
        if (intent.resolveActivity(context.packageManager) != null) {
            return true
        }

        DialogView(context).apply {
            setTitle(context.getString(R.string.title_error))
            setMessage(
                context.getString(
                    R.string.error_no_translator_app_is_not_found
                )
            )
            setDialogType(DialogView.DialogType.CANCEL_ONLY)
        }.attachToScreen()

        return false
    }
}
