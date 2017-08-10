package tw.firemaples.onscreenocr.database;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.BuildConfig;

/**
 * Created by louis1chen on 01/05/2017.
 */

public class TranslateServiceModel {
    private static final String KEY = "TranslateService";
    private static final String DEBUG_KEY = "TranslateServiceDebug";

    public int current;

    public List<ServiceModel> services = new ArrayList<>();

    public TranslateServiceEnum getCurrent() {
        return services.get(current).name;
    }

    public static String getKey() {
        if (BuildConfig.DEBUG) {
            return DEBUG_KEY;
        } else {
            return KEY;
        }
    }

    public ServiceModel getServiceModel(TranslateServiceEnum translateServiceEnum) {
        for (ServiceModel service : services) {
            if (service.name == translateServiceEnum) {
                return service;
            }
        }
        return null;
    }

    public ServiceModel getCurrentServiceModel() {
        getServiceModel(getCurrent());
        return getServiceModel(getCurrent());
    }

    public enum TranslateServiceEnum {
        google, microsoft
    }
}
