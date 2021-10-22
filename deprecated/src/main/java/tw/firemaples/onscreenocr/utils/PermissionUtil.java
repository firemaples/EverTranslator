package tw.firemaples.onscreenocr.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by firemaples on 22/01/2017.
 */

public class PermissionUtil {
    /**
     * Reference: <a href="https://stackoverflow.com/a/46174872/2906153">https://stackoverflow.com/a/46174872/2906153</a><br/>
     * Update >= to > because XiaoMi 8 (MIUI 10.0.2.0 Android 8.1) has the same issue
     */
    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) { //Update >= to > because XiaoMi 8 (MIUI 10.0.2.0 Android 8.1) has the same issue
            return Settings.canDrawOverlays(context);
        } else {
            if (Settings.canDrawOverlays(context)) {
                return true;
            }
            try {
                WindowManager mgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (mgr == null) {
                    return false; //getSystemService might return null
                }
                View viewToAdd = new View(context);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(0, 0, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
                viewToAdd.setLayoutParams(params);
                mgr.addView(viewToAdd, params);
                mgr.removeView(viewToAdd);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
