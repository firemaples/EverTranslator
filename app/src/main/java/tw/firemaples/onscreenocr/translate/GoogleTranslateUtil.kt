package tw.firemaples.onscreenocr.translate

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.floatingviews.screencrop.DialogView
import tw.firemaples.onscreenocr.translate.event.InstallGoogleTranslatorEvent
import tw.firemaples.onscreenocr.utils.FabricUtils
import tw.firemaples.onscreenocr.utils.Utils
import tw.firemaples.onscreenocr.utils.asStringRes

private const val PACKAGE_NAME_GOOGLE_TRANSLATE = "com.google.android.apps.translate"

class GoogleTranslateUtil {
    companion object {
        @JvmStatic
        fun isInstalled(): Boolean =
                Utils.isPackageInstalled(PACKAGE_NAME_GOOGLE_TRANSLATE)

        @JvmStatic
        fun getGoogleTranslateInfo(): GoogleTranslateInfo? =
                Utils.getPackageInfo(PACKAGE_NAME_GOOGLE_TRANSLATE)?.let {
                    GoogleTranslateInfo(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                                it.longVersionCode
                            else
                                it.versionCode.toLong(),
                            it.versionName)
                }

        @JvmStatic
        fun checkInstalled(context: Context): Boolean {
            if (isInstalled()) return true
            DialogView(context).apply {
                reset()
                setType(DialogView.Type.CONFIRM_CANCEL)
                setTitle(context.getString(R.string.title_googleTranslateAppNotInstalled))
                setContentMsg(context.getString(R.string.msg_googleTranslteAppNotInstalled))
                okBtn.text = context.getString(R.string.install)
                setCallback(object : DialogView.OnDialogViewCallback() {
                    override fun onConfirmClick(dialogView: DialogView) {
                        super.onConfirmClick(dialogView)
                        if (Utils.openPlayStore(PACKAGE_NAME_GOOGLE_TRANSLATE)) {
                            EventUtil.post(InstallGoogleTranslatorEvent())
                        }
                    }
                })
            }.attachToWindow()

            return false
        }

        @JvmStatic
        fun start(context: Context, langTo: String, text: String) {
            try {
                context.startActivity(Intent().apply {
                    type = "text/plain"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        action = Intent.ACTION_PROCESS_TEXT
                        putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                        putExtra("key_language_to", langTo)
                        setPackage("com.google.android.apps.translate")
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
                                "com.google.android.apps.translate",
                                //Change is here
                                //"com.google.android.apps.translate.HomeActivity"));
                                "com.google.android.apps.translate.TranslateActivity")
                    }
                })
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                FabricUtils.logGoogleTranslateNotFoundWhenResult()
                Utils.showSimpleDialog(R.string.dialog_title_error.asStringRes(),
                        R.string.error_googleTranslatorNotInstalled.asStringRes())
            }
        }
    }
}

data class GoogleTranslateInfo(val versionCode: Long, val versionName: String)