package tw.firemaples.onscreenocr.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by louis1chen on 26/03/2018.
 */

public class UIUtil {
    public static int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }
}
