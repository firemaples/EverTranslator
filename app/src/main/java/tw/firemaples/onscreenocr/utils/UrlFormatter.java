package tw.firemaples.onscreenocr.utils;

import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;

import tw.firemaples.onscreenocr.database.DatabaseManager;
import tw.firemaples.onscreenocr.database.ServiceHolderModel;
import tw.firemaples.onscreenocr.database.ServiceModel;

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
        if (serviceModel != null) {
            return serviceModel.url;
        } else {
            String fullJson = serviceHolder.toJsonString();
            Crashlytics.log("TranslateServiceHolder: " + fullJson);
            Crashlytics.setString("serviceName", serviceName);
            Crashlytics.logException(new Exception("Can't not get service with service name [" + serviceName + "]"));
            return "";
        }
    }

    public static String getFormattedUrl(String serviceName, @NonNull String text, @NonNull String targetLanguage) {
        return getFormattedUrl(serviceName, text, "", targetLanguage);
    }

    public static String getFormattedUrl(String serviceName, @NonNull String text, @NonNull String sourceLanguage, @NonNull String targetLanguage) {
        return getUrlFormat(serviceName).replace(FORMAT_TEXT, text).replace(FORMAT_SOURCE_LANG, sourceLanguage).replace(FORMAT_TARGET_LANG, targetLanguage);
    }
}
