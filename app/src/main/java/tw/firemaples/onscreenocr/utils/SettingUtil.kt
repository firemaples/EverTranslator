package tw.firemaples.onscreenocr.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.preference.PreferenceManager
import tw.firemaples.onscreenocr.BuildConfig
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.floatingviews.screencrop.HelpView
import tw.firemaples.onscreenocr.floatingviews.screencrop.VersionHistoryView
import tw.firemaples.onscreenocr.receivers.SamsungSpenInsertedReceiver
import java.util.*

object SettingUtil {
    private const val KEY_DEVICE_ID = "KEY_DEVICE_ID"
    private const val KEY_DEBUG_MODE = "KEY_DEBUG_MODE"
    private const val KEY_APP_SHOWING = "KEY_APP_SHOWING"
    private const val KEY_ENABLE_TRANSLATION = "KEY_ENABLE_TRANSLATION"
    private const val KEY_STARTING_WITH_SELECTION_MODE = "KEY_STARTING_WITH_SELECTION_MODE"
    private const val KEY_REMOVE_LINE_BREAKS = "KEY_REMOVE_LINE_BREAKS"
    private const val KEY_AUTO_COPY_OCR_RESULT = "KEY_AUTO_COPY_OCR_RESULT"
    private const val KEY_AUTO_CLOSE_APP_WHEN_SPEN_INSERTED = "KEY_AUTO_CLOSE_APP_WHEN_SPEN_INSERTED"
    private const val KEY_READ_SPEED_ENABLE = "KEY_READ_SPEED_ENABLE"
    private const val KEY_READ_SPEED = "KEY_READ_SPEED"
    private const val KEY_REMEMBER_LAST_SELECTION = "KEY_REMEMBER_LAST_SELECTION"
    private const val KEY_LAST_SELECTION_AREA = "KEY_LAST_SELECTION_AREA"
    private const val KEY_VERSION_HISTORY_SHOWN_VERSION = "KEY_VERSION_HISTORY_SHOWN_VERSION"
    private const val KEY_HOW_TO_USE_SHOWN_VERSION = "KEY_HOW_TO_USE_SHOWN_VERSION"
    private const val KEY_LAST_MAIN_BAR_POSITION_X = "KEY_LAST_MAIN_BAR_POSITION_X"
    private const val KEY_LAST_MAIN_BAR_POSITION_Y = "KEY_LAST_MAIN_BAR_POSITION_Y"
    private const val KEY_FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL_SEC = "KEY_FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL_SEC"

    private val context: Context
        get() {
            return CoreApplication.instance
        }

    private val sp: SharedPreferences
            by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    val deviceId: String by lazy {
        var deviceId = sp.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            sp.edit().putString(KEY_DEVICE_ID, deviceId).apply()
            deviceId
        } else deviceId
    }

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

    var autoCopyOCRResult: Boolean
        get() {
            return sp.getBoolean(KEY_AUTO_COPY_OCR_RESULT, false)
        }
        set(value) {
            sp.edit().putBoolean(KEY_AUTO_COPY_OCR_RESULT, value).apply()
        }

    var autoCloseAppWhenSpenInserted: Boolean
        get() {
            return sp.getBoolean(KEY_AUTO_CLOSE_APP_WHEN_SPEN_INSERTED, true)
        }
        set(value) {
            sp.edit().putBoolean(KEY_AUTO_CLOSE_APP_WHEN_SPEN_INSERTED, value).apply()
            if (value) {
                SamsungSpenInsertedReceiver.start()
            } else {
                SamsungSpenInsertedReceiver.stop()
            }
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
        get() {
            val versionName = VersionHistoryView.getLastHistoryVersion(context)

            val shownVersion = sp.getString(KEY_VERSION_HISTORY_SHOWN_VERSION, null)
            val result = shownVersion?.equals(versionName, ignoreCase = true) == true
            if (!result) {
                sp.edit().putString(KEY_VERSION_HISTORY_SHOWN_VERSION, versionName).apply()
            }
            return result
        }

    val isReadmeAlreadyShown: Boolean
        get() {
            val currentVersion = HelpView.VERSION

            val result = sp.getString(KEY_HOW_TO_USE_SHOWN_VERSION, null) == currentVersion
            if (!result) {
                sp.edit().putString(KEY_HOW_TO_USE_SHOWN_VERSION, currentVersion).apply()
            }
            return result
        }

    var lastMainBarPosition: Array<Int>
        get() = arrayOf(
                sp.getInt(KEY_LAST_MAIN_BAR_POSITION_X, -1),
                sp.getInt(KEY_LAST_MAIN_BAR_POSITION_Y, -1))
        set(value) {
            sp.edit().putInt(KEY_LAST_MAIN_BAR_POSITION_X, value[0])
                    .putInt(KEY_LAST_MAIN_BAR_POSITION_Y, value[1]).apply()
        }

    var firebaseRemoteConfigFetchInterval: Long
        get() = sp.getLong(KEY_FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL_SEC, 43200)
        set(value) {
            sp.edit().putLong(KEY_FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL_SEC, value)
        }
}