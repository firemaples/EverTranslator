package tw.firemaples.onscreenocr.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by louis1chen on 22/01/2017.
 */

public class PermissionUtil {
    public static boolean checkDrawOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else {
            return true;
        }
    }
}
