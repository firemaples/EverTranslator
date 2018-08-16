package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import tw.firemaples.onscreenocr.CoreApplication

abstract class BaseSettingUtil {
    protected val context: Context by lazy { CoreApplication.instance }

    protected val sp: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
}