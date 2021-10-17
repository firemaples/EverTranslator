package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import tw.firemaples.onscreenocr.CoreApplication

object Utils {
    private val logger: Logger by lazy { Logger(Utils::class) }

    val context: Context by lazy { CoreApplication.instance }

    @Throws(PackageManager.NameNotFoundException::class)
    fun isPackageInstalled(packageName: String): Boolean =
        getPackageInfo(packageName) != null

    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageInfo(packageName: String): PackageInfo? =
        try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (e: Exception) {
            logger.warn(t = e)
            null
        }

    fun openBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            ContextCompat.startActivity(context, intent, null)
        } catch (e: Exception) {
            logger.warn("Unable to open a URL in browser", e)
        }
    }
}
