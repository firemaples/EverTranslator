package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
}
