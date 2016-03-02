package tw.firemaples.onscreenocr.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

/**
 * Created by Louis on 2016/3/1.
 */
public class Tool {
    private static String LOG_TAG = "OnScreenOcr";
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void LogError(String msg){
        Log.e(LOG_TAG, msg);
    }

    public static void LogInfo(String msg){
        Log.i(LOG_TAG, msg);
    }

    public static void ShowMsg(Context context, String msg){
        SuperToast.create(context, msg, SuperToast.Duration.LONG,
                Style.getStyle(Style.GREEN, SuperToast.Animations.FADE)).show();
    }

    public static void ShowErrorMsg(Context context, String msg){
        SuperToast.create(context, msg, SuperToast.Duration.LONG,
                Style.getStyle(Style.RED, SuperToast.Animations.FADE)).show();
    }
}
