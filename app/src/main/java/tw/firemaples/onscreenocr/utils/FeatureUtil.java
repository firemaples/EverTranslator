package tw.firemaples.onscreenocr.utils;

import tw.firemaples.onscreenocr.BuildConfig;

/**
 * Created by louis1chen on 11/02/2017.
 */

public class FeatureUtil {
    private static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    public static boolean isNewModeEnabled() {
//        return isDebuggable();
        return false;
    }
}
