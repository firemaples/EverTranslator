package tw.firemaples.onscreenocr.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.preference.PreferenceManager
import tw.firemaples.onscreenocr.BuildConfig
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.floatingviews.screencrop.VersionHistoryView

object SettingUtil {
    private const val KEY_DEBUG_MODE = "KEY_DEBUG_MODE"
    private const val KEY_APP_SHOWING = "KEY_APP_SHOWING"
    private const val KEY_ENABLE_TRANSLATION = "KEY_ENABLE_TRANSLATION"
    private const val KEY_STARTING_WITH_SELECTION_MODE = "KEY_STARTING_WITH_SELECTION_MODE"
    private const val KEY_REMOVE_LINE_BREAKS = "KEY_REMOVE_LINE_BREAKS"
    private const val KEY_READ_SPEED_ENABLE = "KEY_READ_SPEED_ENABLE"
    private const val KEY_READ_SPEED = "KEY_READ_SPEED"
    private const val KEY_REMEMBER_LAST_SELECTION = "KEY_REMEMBER_LAST_SELECTION"
    private const val KEY_LAST_SELECTION_AREA = "KEY_LAST_SELECTION_AREA"
    private const val KEY_VERSION_HISTORY_SHOWN_VERSION = "KEY_VERSION_HISTORY_SHOWN_VERSION"
    private const val KEY_HOW_TO_USE_SHOWN_VERSION = "KEY_HOW_TO_USE_SHOWN_VERSION"
    private const val KEY_LITE_HOW_TO_USE_SHOWN_VERSION = "KEY_LITE_HOW_TO_USE_SHOWN_VERSION"

    private val context: Context
        get() {
            return CoreApplication.instance
        }

    private val sp: SharedPreferences
            by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    var isDebugMode: Boolean
        get() {
            return BuildConfig.DEBUG && sp.getBoolean(KEY_DEBUG_MODE, false)
        }
        set(value) {
            sp.edit().putBoolean(KEY_DEBUG_MODE, value).apply()
        }

    var isAppShowing: Boolean
        get() {
            return sp.getBoolean(KEY_APP_SHOWING, true)
        }
        set(value) {
            sp.edit().putBoolean(KEY_APP_SHOWING, value).apply()
        }

    var enableTranslation: Boolean
        get() {
            return sp.getBoolean(KEY_ENABLE_TRANSLATION, true)
        }
        set(value) {
            sp.edit().putBoolean(KEY_ENABLE_TRANSLATION, value).apply()
        }

    var startingWithSelectionMode: Boolean
        get() {
            return sp.getBoolean(KEY_STARTING_WITH_SELECTION_MODE, true)
        }
        set(value) {
            sp.edit().putBoolean(KEY_STARTING_WITH_SELECTION_MODE, value).apply()
        }

    var removeLineBreaks: Boolean
        get() {
            return sp.getBoolean(KEY_REMOVE_LINE_BREAKS, true)
        }
        set(value) {
            sp.edit().putBoolean(KEY_REMOVE_LINE_BREAKS, value).apply()
        }

    var readSpeedEnabled: Boolean
        get() {
            return sp.getBoolean(KEY_READ_SPEED_ENABLE, false)
        }
        @SuppressLint("ApplySharedPref")
        set(value) {
            sp.edit().putBoolean(KEY_READ_SPEED_ENABLE, value).commit()
        }

    var readSpeed: Float
        get() {
            return sp.getFloat(KEY_READ_SPEED, 0.6f)
        }
        set(value) {
            sp.edit().putFloat(KEY_READ_SPEED, value).apply()
        }

    var isRememberLastSelection: Boolean
        get() {
            return sp.getBoolean(KEY_REMEMBER_LAST_SELECTION, true)
        }
        set(value) {
            sp.edit().putBoolean(KEY_REMEMBER_LAST_SELECTION, value).apply()
        }

    var lastSelectionArea: List<Rect>
        get() {
            return sp.getString(KEY_LAST_SELECTION_AREA, null)?.let {
                JsonUtil<List<Rect>>().parseJson(it, object : TypeReference<List<Rect>>() {

                })
            } ?: ArrayList()
        }
        set(value) {
            JsonUtil<List<Rect>>().writeJson(value).let {
                sp.edit().putString(KEY_LAST_SELECTION_AREA, it).apply()
            }
        }

    val isVersionHistoryAlreadyShown: Boolean
        @SuppressLint("ApplySharedPref")
        get() {
            val versionName = VersionHistoryView.getLastHistoryVersion(context)

            val shownVersion = sp.getString(KEY_VERSION_HISTORY_SHOWN_VERSION, null)
            val result = shownVersion?.equals(versionName, ignoreCase = true) == true
            if (!result) {
                sp.edit().putString(KEY_VERSION_HISTORY_SHOWN_VERSION, versionName).commit()
            }
            return result
        }
}