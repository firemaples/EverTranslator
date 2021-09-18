package tw.firemaples.onscreenocr.utils

import android.view.Window
import notchtools.geek.com.notchtools.NotchTools
import tw.firemaples.onscreenocr.pref.AppPref

object NotchUtil {
    fun check(window: Window) {
        val notchTools = NotchTools.getFullScreenTools()

        with(AppPref) {
            hasNotch = notchTools.isNotchScreen(window)
            notchHeight = notchTools.getNotchHeight(window)
            statusBarHeight = notchTools.getStatusHeight(window)
        }
    }

    fun getTopOffset(): Int =
        if (UIUtils.isPortrait && UIUtils.isStatusBarTakingVerticalSpace)
            AppPref.statusBarHeight else 0

    fun getLeftOffset(): Int =
        if (!UIUtils.isPortrait && UIUtils.isStatusBarTakingHorizontalSpace)
            AppPref.statusBarHeight else 0
}
