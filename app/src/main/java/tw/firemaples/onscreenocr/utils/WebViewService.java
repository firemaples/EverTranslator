package tw.firemaples.onscreenocr.utils;

import android.support.annotation.NonNull;

/**
 * Created by firemaples on 30/11/2016.
 */

public class WebViewService {
    private final static String FORMAT_TEXT = "{TEXT}";
    private final static String FORMAT_SOURCE_LANG = "{SL}";
    private final static String FORMAT_TARGET_LANG = "{TL}";

    public enum Type {
        Google("https://translate.google.com/?sl=auto&tl=" + FORMAT_TARGET_LANG + "&q=" + FORMAT_TEXT);

        private final String urlFormat;

        Type(String urlFormat) {
            this.urlFormat = urlFormat;
        }

        public String getFormattedUrl(@NonNull String text, @NonNull String targetLanguage) {
            return getFormattedUrl(text, "", targetLanguage);
        }

        public String getFormattedUrl(@NonNull String text, @NonNull String sourceLanguage, @NonNull String targetLanguage) {
            return urlFormat.replace(FORMAT_TEXT, text).replace(FORMAT_SOURCE_LANG, sourceLanguage).replace(FORMAT_TARGET_LANG, targetLanguage);
        }
    }
}
