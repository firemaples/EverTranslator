package tw.firemaples.onscreenocr.utils;

import android.support.annotation.NonNull;

import tw.firemaples.onscreenocr.database.DatabaseManager;
import tw.firemaples.onscreenocr.database.ServiceModel;
import tw.firemaples.onscreenocr.database.ServiceHolderModel;

/**
 * Created by firemaples on 30/11/2016.
 */

public class UrlFormatter {
    private static String YANDEX_API_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?key={KEY}&text={TEXT}&lang={TL}";

    private final static String FORMAT_KEY = "{KEY}";
    private final static String FORMAT_TEXT = "{TEXT}";
    private final static String FORMAT_SOURCE_LANG = "{SL}";
    private final static String FORMAT_TARGET_LANG = "{TL}";

    private static String getUrlFormat(String serviceName) {
        ServiceHolderModel serviceHolder = DatabaseManager.getInstance().getTranslateServiceHolder();

        if (ServiceHolderModel.SERVICE_YANDEX_API.equals(serviceName)) {
            ServiceModel service = serviceHolder.getService(ServiceHolderModel.SERVICE_YANDEX_API);

            return YANDEX_API_URL.replace(FORMAT_KEY, service.key);
        }

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
