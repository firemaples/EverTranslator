package tw.firemaples.onscreenocr.utils;

import androidx.annotation.NonNull;

/**
 * Created by firemaples on 30/11/2016.
 */

public class UrlFormatter {

    private final static String FORMAT_KEY = "{KEY}";
    private final static String FORMAT_TEXT = "{TEXT}";
    private final static String FORMAT_SOURCE_LANG = "{SL}";
    private final static String FORMAT_TARGET_LANG = "{TL}";

    public static String getFormattedUrl(String url, @NonNull String text, @NonNull String targetLanguage) {
        return getFormattedUrl(url, text, "", targetLanguage);
    }

    public static String getFormattedUrl(String url, @NonNull String text, @NonNull String sourceLanguage, @NonNull String targetLanguage) {
        return url.replace(FORMAT_TEXT, text).replace(FORMAT_SOURCE_LANG, sourceLanguage).replace(FORMAT_TARGET_LANG, targetLanguage);
    }
}
