package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowManager

object PermissionUtil {
    private val logger: Logger by lazy { Logger(PermissionUtil::class) }

    /**
     * Reference: <a href="https://stackoverflow.com/a/46174872/2906153">https://stackoverflow.com/a/46174872/2906153</a><br/>
     * Update >= to > because XiaoMi 8 (MIUI 10.0.2.0 Android 8.1) has the same issue
     */
    fun canDrawOverlays(context: Context): Boolean {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M ->
                return true

            // Use > instead of >= since XiaoMi 8 (MIUI 10.0.2.0 Android 8.1) has the same issue
            Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1 ->
                return Settings.canDrawOverlays(context)

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    Settings.canDrawOverlays(context) ->
                return true

            else -> {
                try {
                    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                    if (windowManager == null) {
                        logger.warn("The window service is not available")
                        return false
                    }

                    val viewToAdd = View(context)
                    val type =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                    val flags =
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    val params = WindowManager.LayoutParams(0, 0, type, flags, PixelFormat.TRANSPARENT)
                    viewToAdd.layoutParams = params
                    windowManager.addView(viewToAdd, params)
                    windowManager.removeView(viewToAdd)
                    return true
                } catch (e: Exception) {
                    logger.warn(t = e)
                    return false
                }
            }
        }
    }
}
