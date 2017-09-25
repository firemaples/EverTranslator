/*
 * Copyright 2016-2017 Louis Chen [firemaples@gmail.com].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tw.firemaples.onscreenocr.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
}
