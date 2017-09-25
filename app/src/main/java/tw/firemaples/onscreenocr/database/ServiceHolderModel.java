package tw.firemaples.onscreenocr.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tw.firemaples.onscreenocr.BuildConfig;
import tw.firemaples.onscreenocr.utils.JsonUtil;

/**
 * Created by firemaples on 01/05/2017.
 */

public class ServiceHolderModel {
    private static final String KEY = "TranslateService-V2";
    private static final String DEBUG_KEY = "TranslateServiceDebug";

    public static final String SERVICE_GOOGLE_WEB_API = "google-web-api";
    public static final String SERVICE_GOOGLE_WEB = "google-web";
    public static final String SERVICE_MICROSOFT_API = "microsoft-api";
    public static final String SERVICE_YANDEX_API = "yandex-api";

    public static final List<String> supportedServiceNames = Arrays.asList(
            SERVICE_GOOGLE_WEB_API,
            SERVICE_GOOGLE_WEB,
            SERVICE_MICROSOFT_API,
            SERVICE_YANDEX_API
    );

    @SuppressWarnings("unused")
    @Deprecated
    public int current;

    public List<String> usingOrder = new ArrayList<>();

    public List<ServiceModel> services = new ArrayList<>();

    ServiceModel currentService;

    public String ocrDataUrl;

    public ServiceModel getUsingService() {
        if (currentService == null) {
            currentService = getUsingService(null);
        }
        return currentService;
    }

    public ServiceModel switchNextService(boolean resetIfNull) {
        if (currentService == null) {
            if (resetIfNull) {
                return currentService = getUsingService(null);
            } else {
                return null;
            }
        }
        currentService = getUsingService(currentService.name);
        if (currentService == null && resetIfNull) {
            switchNextService(true);
        }
        return currentService;
    }

    public ServiceModel getUsingService(String nextOf) {
        int indexStart;
        if (nextOf == null || nextOf.trim().length() == 0) {
            indexStart = -1;
        } else {
            indexStart = usingOrder.indexOf(nextOf);
            if (indexStart < 0 || indexStart == usingOrder.size() - 1) {
                return null;
            }
        }

        for (int i = indexStart + 1; i < usingOrder.size(); i++) {
            String serviceName = usingOrder.get(i);
            if (supportedServiceNames.contains(serviceName)) {
                return getService(serviceName);
            }
        }

        return null;
    }

    public ServiceModel getService(String serviceName) {
        for (ServiceModel service : services) {
            if (service.name.equals(serviceName)) {
                return service;
            }
        }
        return null;
    }

    public static String getKey() {
        if (BuildConfig.DEBUG) {
            return DEBUG_KEY;
        } else {
            return KEY;
        }
    }

    public String toJsonString() {
        return new JsonUtil<ServiceHolderModel>().writeJson(this);
    }
}
