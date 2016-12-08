package tw.firemaples.onscreenocr.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import tw.firemaples.onscreenocr.ScreenTranslatorService;

/**
 * Created by Louis on 2016/3/1.
 */
public class Tool {
    private static final String KEY_DEBUG_MODE = "KEY_DEBUG_MODE";
    private static final String KEY_ENABLE_TRANSLATION = "KEY_ENABLE_TRANSLATION";
    private static Tool _instance;

    private static String LOG_TAG = "OnScreenOcr";

    public static void init() {
        _instance = new Tool();
    }

    public static Tool getInstance() {
        return _instance;
    }

    private Tool() {
    }

    public static Context getContext() {
        return ScreenTranslatorService.getContext();
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void logError(String msg) {
        Log.e(LOG_TAG, msg);
    }

    public static void logInfo(String msg) {
        Log.i(LOG_TAG, msg);
    }

    public void showMsg(String msg) {
        if (getContext() == null) {
            return;
        }
        SuperToast.cancelAllSuperToasts();
        SuperToast.create(getContext(), msg, SuperToast.Duration.VERY_SHORT,
                Style.getStyle(Style.BLACK, SuperToast.Animations.FADE)).show();
    }

    public void showErrorMsg(String msg) {
        if (getContext() == null) {
            return;
        }
        SuperToast.cancelAllSuperToasts();
        SuperToast.create(getContext(), msg, SuperToast.Duration.VERY_SHORT,
                Style.getStyle(Style.RED, SuperToast.Animations.FADE)).show();
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public boolean isDebugMode() {
        return getSharedPreferences().getBoolean(KEY_DEBUG_MODE, false);
    }

    public void setDebugMode(boolean debugMode) {
        getSharedPreferences().edit().putBoolean(KEY_DEBUG_MODE, debugMode).apply();
    }

    public boolean isEnableTranslation() {
        return getSharedPreferences().getBoolean(KEY_ENABLE_TRANSLATION, true);
    }

    public void setEnableTranslation(boolean enableTranslation) {
        getSharedPreferences().edit().putBoolean(KEY_ENABLE_TRANSLATION, enableTranslation).apply();
    }

    public static String replaceAllLineBreaks(String str, String replaceWith) {
        return str.replace("\r\n", replaceWith).replace("\r", replaceWith).replace("\n", replaceWith);
    }

    public void openBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(intent);
        }
    }
}
