package tw.firemaples.onscreenocr.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by louis1chen on 01/02/2017.
 */
public class SharePreferenceUtil {
    private static final String KEY_DEBUG_MODE = "KEY_DEBUG_MODE";
    private static final String KEY_APP_SHOWING = "KEY_APP_SHOWING";
    private static final String KEY_ENABLE_TRANSLATION = "KEY_ENABLE_TRANSLATION";
    private static final String KEY_STARTING_WITH_SELECTION_MODE = "KEY_STARTING_WITH_SELECTION_MODE";
    private static final String KEY_REMOVE_LINE_BREAKS = "KEY_REMOVE_LINE_BREAKS";
    private static final String KEY_APP_MODE = "KEY_APP_MODE";
    private static final String KEY_READ_SPEED_ENABLE = "KEY_READ_SPEED_ENABLE";
    private static final String KEY_READ_SPEED = "KEY_READ_SPEED";

    private static SharePreferenceUtil ourInstance = new SharePreferenceUtil();

    public static SharePreferenceUtil getInstance() {
        return ourInstance;
    }

    private SharePreferenceUtil() {
    }

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferences(Tool.getContext());
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isDebugMode() {
        return getSharedPreferences().getBoolean(KEY_DEBUG_MODE, false);
    }

    public void setDebugMode(boolean debugMode) {
        getSharedPreferences().edit().putBoolean(KEY_DEBUG_MODE, debugMode).apply();
    }

    public boolean isAppShowing() {
        return getSharedPreferences().getBoolean(KEY_APP_SHOWING, true);
    }

    public void setIsAppShowing(boolean isAppShowing) {
        setIsAppShowing(isAppShowing, Tool.getContext());
    }

    public void setIsAppShowing(boolean isAppShowing, Context context) {
        getSharedPreferences(context).edit().putBoolean(KEY_APP_SHOWING, isAppShowing).apply();
    }

    public boolean isEnableTranslation() {
        return getSharedPreferences().getBoolean(KEY_ENABLE_TRANSLATION, true);
    }

    public void setEnableTranslation(boolean enableTranslation) {
        getSharedPreferences().edit().putBoolean(KEY_ENABLE_TRANSLATION, enableTranslation).apply();
    }

    public boolean startingWithSelectionMode() {
        return getSharedPreferences().getBoolean(KEY_STARTING_WITH_SELECTION_MODE, true);
    }

    public void setStartingWithSelectionMode(boolean startingWithSelectionMode) {
        getSharedPreferences().edit().putBoolean(KEY_STARTING_WITH_SELECTION_MODE, startingWithSelectionMode).apply();
    }

    public boolean removeLineBreaks() {
        return getSharedPreferences().getBoolean(KEY_REMOVE_LINE_BREAKS, true);
    }

    public void setRemoveLineBreaks(boolean removeLineBreaks) {
        getSharedPreferences().edit().putBoolean(KEY_REMOVE_LINE_BREAKS, removeLineBreaks).apply();
    }

    public AppMode getAppMode() {
        return AppMode.valueOf(getSharedPreferences().getString(KEY_APP_MODE, AppMode.ScreenCrop.name()));
    }

    public void setAppMode(AppMode appMode) {
        getSharedPreferences().edit().putString(KEY_APP_MODE, appMode.name()).apply();
    }

    public boolean getReadSpeedEnable() {
        return getSharedPreferences().getBoolean(KEY_READ_SPEED_ENABLE, false);
    }

    @SuppressLint("ApplySharedPref")
    public void setReadSpeedEnable(boolean enable) {
        getSharedPreferences().edit().putBoolean(KEY_READ_SPEED_ENABLE, enable).commit();
    }

    public float getReadSpeed() {
        return getSharedPreferences().getFloat(KEY_READ_SPEED, 0.6f);
    }

    public void setReadSpeed(float speed) {
        getSharedPreferences().edit().putFloat(KEY_READ_SPEED, speed).apply();
    }
}
