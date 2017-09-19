package tw.firemaples.onscreenocr.utils;

import android.support.annotation.NonNull;

import tw.firemaples.onscreenocr.database.DatabaseManager;
import tw.firemaples.onscreenocr.database.ServiceModel;
import tw.firemaples.onscreenocr.database.ServiceHolderModel;

/**
 * Created by firemaples on 30/11/2016.
 */

public class GoogleWebViewUtil {
    private final static String FORMAT_TEXT = "{TEXT}";
    private final static String FORMAT_SOURCE_LANG = "{SL}";
    private final static String FORMAT_TARGET_LANG = "{TL}";

    private static String getUrlFormat(String serviceName) {
        ServiceHolderModel serviceHolder = DatabaseManager.getInstance().getTranslateServiceHolder();
        ServiceModel serviceModel = serviceHolder.getService(serviceName);
        return serviceModel.url;
    }

    public static String getFormattedUrl(String serviceName, @NonNull String text, @NonNull String targetLanguage) {
        return getFormattedUrl(serviceName, text, "", targetLanguage);
    }

    public static String getFormattedUrl(String serviceName, @NonNull String text, @NonNull String sourceLanguage, @NonNull String targetLanguage) {
        return getUrlFormat(serviceName).replace(FORMAT_TEXT, text).replace(FORMAT_SOURCE_LANG, sourceLanguage).replace(FORMAT_TARGET_LANG, targetLanguage);
    }
}
