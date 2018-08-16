package tw.firemaples.onscreenocr.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import com.muddzdev.styleabletoastlibrary.StyleableToast
import tw.firemaples.onscreenocr.CoreApplication

class Utils {
    companion object {
        val context: Context
            get() {
                return CoreApplication.instance
            }

        private val baseToast: StyleableToast.Builder
            get() {
                return StyleableToast.Builder(context)
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLACK)
                        .length(Toast.LENGTH_LONG)
            }

        @JvmStatic
        fun isServiceRunning(serviceClass: Class<*>): Boolean =
                (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?)
                        ?.getRunningServices(Int.MAX_VALUE)
                        ?.any { serviceClass.name == it.service.className } == true

        @JvmStatic
        fun showToast(msg: String) =
                baseToast.text(msg).show()

        @JvmStatic
        fun showErrorToast(msg: String) =
                baseToast.text(msg).backgroundColor(Color.RED).show()

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
        fun openPlayStore(context: Context, packageName: String) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            intent.resolveActivity(context.packageManager)?.let {
                context.startActivity(intent)
            }
        }

        @JvmStatic
        fun isPackageInstalled(context: Context, packageName: String): Boolean =
                try {
                    context.packageManager.getPackageInfo(packageName, 0)
                    true
                } catch (e: PackageManager.NameNotFoundException) {
                    false
                }
    }
}