package tw.firemaples.onscreenocr.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.BuildConfig;
import tw.firemaples.onscreenocr.floatingviews.screencrop.HelpView;
import tw.firemaples.onscreenocr.floatingviews.screencrop.VersionHistoryView;

/**
 * Created by firemaples on 01/02/2017.
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
    private static final String KEY_REMEMBER_LAST_SELECTION = "KEY_REMEMBER_LAST_SELECTION";
    private static final String KEY_LAST_SELECTION_AREA = "KEY_LAST_SELECTION_AREA";
    private static final String KEY_VERSION_HISTORY_SHOWN_VERSION = "KEY_VERSION_HISTORY_SHOWN_VERSION";
    private static final String KEY_HOW_TO_USE_SHOWN_VERSION = "KEY_HOW_TO_USE_SHOWN_VERSION";

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
        return BuildConfig.DEBUG && getSharedPreferences().getBoolean(KEY_DEBUG_MODE, false);
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

    public boolean isRememberLastSelection() {
        return getSharedPreferences().getBoolean(KEY_REMEMBER_LAST_SELECTION, true);
    }

    public void setRememberLastSelection(boolean rememberLastSelection) {
        getSharedPreferences().edit().putBoolean(KEY_REMEMBER_LAST_SELECTION, rememberLastSelection).apply();
    }

    public List<Rect> getLastSelectionArea() {
        String json = getSharedPreferences().getString(KEY_LAST_SELECTION_AREA, null);
        if (json == null) {
            return new ArrayList<>();
        } else {
            return new JsonUtil<List<Rect>>().parseJson(json, new TypeReference<List<Rect>>() {
            });
        }
    }

    public void setLastSelectionArea(List<Rect> lastSelectionArea) {
        String json = new JsonUtil<List<Rect>>().writeJson(lastSelectionArea);
        getSharedPreferences().edit().putString(KEY_LAST_SELECTION_AREA, json).apply();
    }

    @SuppressLint("ApplySharedPref")
    public boolean isVersionHistoryAlreadyShown(Context context) {
        String versionName = VersionHistoryView.getLastHistoryVersion(context);

        String shownVersion = getSharedPreferences().getString(KEY_VERSION_HISTORY_SHOWN_VERSION, null);
        boolean result = shownVersion != null && shownVersion.equalsIgnoreCase(versionName);
        if (!result) {
            getSharedPreferences().edit().putString(KEY_VERSION_HISTORY_SHOWN_VERSION, versionName).commit();
        }
        return result;
    }

    @SuppressLint("ApplySharedPref")
    public boolean isHowToUseAlreadyShown() {
        String versionName = HelpView.VERSION;

        String shownVersion = getSharedPreferences().getString(KEY_HOW_TO_USE_SHOWN_VERSION, null);
        boolean result = shownVersion != null && shownVersion.equalsIgnoreCase(versionName);
        if (!result) {
            getSharedPreferences().edit().putString(KEY_HOW_TO_USE_SHOWN_VERSION, versionName).commit();
        }
        return result;
    }
}
