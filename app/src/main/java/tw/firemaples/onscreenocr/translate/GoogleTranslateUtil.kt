package tw.firemaples.onscreenocr.translate

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.floatingviews.screencrop.DialogView
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.translate.event.InstallGoogleTranslatorEvent
import tw.firemaples.onscreenocr.utils.Utils
import tw.firemaples.onscreenocr.utils.asString

private const val PACKAGE_NAME_GOOGLE_TRANSLATE = "com.google.android.apps.translate"

class GoogleTranslateUtil {
    companion object {
        private val logger = LoggerFactory.getLogger(GoogleTranslateUtil::class.java)

        @JvmStatic
        fun isInstalled(): Boolean =
                Utils.isPackageInstalled(PACKAGE_NAME_GOOGLE_TRANSLATE)

        @JvmStatic
        fun getGoogleTranslateInfo(): GoogleTranslateInfo? =
                Utils.getPackageInfo(PACKAGE_NAME_GOOGLE_TRANSLATE)?.let {
                    @Suppress("DEPRECATION")
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
            showNotInstalledDialog(context)

            return false
        }

        private fun showNotInstalledDialog(context: Context) {
            DialogView(context).apply {
                reset()
                setType(DialogView.Type.CONFIRM_CANCEL)
                setTitle(R.string.dialog_title_error.asString())
                setContentMsg(R.string.error_googleTranslatorNotInstalled.asString())
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
                FirebaseEvent.logShowGoogleTranslateWindow()
                FirebaseEvent.logTranslationTextFinished(TranslationService.GoogleTranslatorApp)
            } catch (e: Throwable) {
                logger.error("Start [Google Translate] failed", e)
                FirebaseEvent.logShowGoogleTranslateWindowFailed(e)
                FirebaseEvent.logTranslationTextFailed(TranslationService.GoogleTranslatorApp)
                showNotInstalledDialog(context)
            }
        }
    }
}

data class GoogleTranslateInfo(val versionCode: Long, val versionName: String)