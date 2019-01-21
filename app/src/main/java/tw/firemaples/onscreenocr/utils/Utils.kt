package tw.firemaples.onscreenocr.utils

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import com.muddzdev.styleabletoastlibrary.StyleableToast
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatingviews.screencrop.DialogView

class Utils {
    companion object {
        const val LABEL_OCR_RESULT = "OCR result"
        const val LABEL_TRANSLATED_TEXT = "Translated text"
        val context: Context
            get() {
                return CoreApplication.instance
            }
        val logger: Logger = LoggerFactory.getLogger(Utils::class.java)

        private val baseToast: StyleableToast.Builder
            get() {
                return StyleableToast.Builder(context)
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLACK)
                        .length(Toast.LENGTH_SHORT)
            }

        @JvmStatic
        fun isServiceRunning(serviceClass: Class<*>): Boolean =
                (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?)
                        ?.getRunningServices(Int.MAX_VALUE)
                        ?.any { serviceClass.name == it.service.className } == true

        @JvmStatic
        fun showToast(stringRes: Int) = showToast(context.getString(stringRes))

        @JvmStatic
        fun showToast(msg: String) = baseToast.text(msg).show()

        @JvmStatic
        fun showErrorToast(stringRes: Int) = showErrorToast(context.getString(stringRes))

        @JvmStatic
        fun showErrorToast(msg: String) = baseToast.text(msg).backgroundColor(Color.RED).show()

        @JvmStatic
        fun showSimpleDialog(title: String, msg: String) {
            DialogView(context).apply {
                reset()
                setType(DialogView.Type.CONFIRM_ONLY)
                setTitle(title)
                setContentMsg(msg)
            }.attachToWindow()
        }

        @JvmStatic
        fun replaceAllLineBreaks(str: String?, replaceWith: String): String? =
                str?.replace("\r\n", replaceWith)
                        ?.replace("\r", replaceWith)
                        ?.replace("\n", replaceWith)

        @JvmStatic
        fun openBrowser(url: String) {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            intent.resolveActivity(context.packageManager)?.let {
                context.startActivity(intent)
            }
        }

        @JvmStatic
        fun openPlayStore(packageName: String): Boolean {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return intent.resolveActivity(context.packageManager)?.let {
                context.startActivity(intent)
                true
            } ?: false
        }

        @JvmStatic
        fun isPackageInstalled(packageName: String): Boolean =
                getPackageInfo(packageName) != null

        @JvmStatic
        fun getPackageInfo(packageName: String): PackageInfo? =
                try {
                    context.packageManager.getPackageInfo(packageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    logger.warn("Package not found", e)
                    null
                }

        fun copyToClipboard(label: String, text: String) {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let {
                it.primaryClip = ClipData.newPlainText(label, text)
                Utils.showToast(String.format(context.getString(R.string.msg_textHasBeenCopied), text))
            }
        }
    }
}