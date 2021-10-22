package tw.firemaples.onscreenocr.utils

import android.view.Window
import notchtools.geek.com.notchtools.NotchTools

object NotchUtil : BaseSettingUtil() {
    private const val KEY_HAS_NOTCH = "has_notch"
    private const val KEY_NOTCH_HEIGHT = "notch_height"
    private const val KEY_STATUS_BAR_HEIGHT = "status_bar_height"

    var hasNotch: Boolean
        get() = sp.getBoolean(KEY_HAS_NOTCH, false)
        set(value) {
            sp.edit().putBoolean(KEY_HAS_NOTCH, value).apply()
        }

    var notchHeight: Int
        get() = sp.getInt(KEY_NOTCH_HEIGHT, 0)
        set(value) {
            sp.edit().putInt(KEY_NOTCH_HEIGHT, value).apply()
        }

    var statusBarHeight: Int
        get() = sp.getInt(KEY_STATUS_BAR_HEIGHT, 0)
        set(value) {
            sp.edit().putInt(KEY_STATUS_BAR_HEIGHT, value).apply()
        }

    fun check(window: Window) {
        val notchTools = NotchTools.getFullScreenTools()

        hasNotch = notchTools.isNotchScreen(window)
        notchHeight = notchTools.getNotchHeight(window)
        statusBarHeight = notchTools.getStatusHeight(window)
    }
}