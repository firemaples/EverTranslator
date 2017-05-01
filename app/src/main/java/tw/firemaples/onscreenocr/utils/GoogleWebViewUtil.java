package tw.firemaples.onscreenocr.utils;

import android.support.annotation.NonNull;

import tw.firemaples.onscreenocr.database.DatabaseManager;
import tw.firemaples.onscreenocr.database.ServiceModel;
import tw.firemaples.onscreenocr.database.TranslateServiceModel;

/**
 * Created by firemaples on 30/11/2016.
 */

public class GoogleWebViewUtil {
    private final static String FORMAT_TEXT = "{TEXT}";
    private final static String FORMAT_SOURCE_LANG = "{SL}";
    private final static String FORMAT_TARGET_LANG = "{TL}";

    private static String getUrlFormat() {
        TranslateServiceModel translateService = DatabaseManager.getInstance().getTranslateService();
        ServiceModel serviceModel = translateService.getServiceModel(TranslateServiceModel.TranslateServiceEnum.google);
        return serviceModel.url;
    }

    public static String getFormattedUrl(@NonNull String text, @NonNull String targetLanguage) {
        return getFormattedUrl(text, "", targetLanguage);
    }

    public static String getFormattedUrl(@NonNull String text, @NonNull String sourceLanguage, @NonNull String targetLanguage) {
        return getUrlFormat().replace(FORMAT_TEXT, text).replace(FORMAT_SOURCE_LANG, sourceLanguage).replace(FORMAT_TARGET_LANG, targetLanguage);
    }
}
