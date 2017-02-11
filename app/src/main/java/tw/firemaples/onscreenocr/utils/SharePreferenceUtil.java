package tw.firemaples.onscreenocr.utils;

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

    private static SharePreferenceUtil ourInstance = new SharePreferenceUtil();

    public static SharePreferenceUtil getInstance() {
        return ourInstance;
    }

    private SharePreferenceUtil() {
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(Tool.getContext());
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
        getSharedPreferences().edit().putBoolean(KEY_APP_SHOWING, isAppShowing).apply();
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
}
