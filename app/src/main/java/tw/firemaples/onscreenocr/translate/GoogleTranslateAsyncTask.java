package tw.firemaples.onscreenocr.translate;

import android.util.Log;
import android.webkit.WebView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.firemaples.onscreenocr.utils.GoogleWebViewUtil;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 13/09/2017.
 */

public class GoogleTranslateAsyncTask {
    private static final String REGEX_RESULT_MATCHER = "TRANSLATED_TEXT='(.[^\\']*)'";

    public void startTranslate(String textToTranslate, String targetLanguage, final OnGoogleTranslateTaskCallback callback) {

        String lang = Locale.forLanguageTag(targetLanguage).getLanguage();
        if (lang.equals(Locale.CHINESE.getLanguage())) {
            lang += "-" + Locale.getDefault().getCountry();
        }

        String url = GoogleWebViewUtil.getFormattedUrl(textToTranslate, lang);

        Tool.logInfo("GoogleTranslateAsyncTask start loading url:" + url);

        String userAgent = new WebView(Tool.getContext()).getSettings().getUserAgentString();
        Tool.logInfo("UserAgent: " + userAgent);
        if (userAgent == null || userAgent.trim().length() == 0) {
            userAgent = "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Mobile Safari/537.36";
        }
        AndroidNetworking.get(url).setPriority(Priority.HIGH)
                .addHeaders("user-agent", userAgent)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Tool.logInfo("GoogleTranslateAsyncTask: onResponse");
                        Pattern pattern = Pattern.compile(REGEX_RESULT_MATCHER);
                        Matcher matcher = pattern.matcher(response);
                        if (matcher.find()) {
                            String translatedText = matcher.group(1);
                            callback.onTranslated(translatedText);
                        } else {
                            callback.onError(new GoogleTranslateResultNotFoundException(response));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Tool.logError("GoogleTranslateAsyncTask: " + Log.getStackTraceString(anError));
                        callback.onError(anError);
                    }
                });
    }

    public interface OnGoogleTranslateTaskCallback {
        void onTranslated(String translatedText);

        void onError(Throwable throwable);
    }

    public static class GoogleTranslateResultNotFoundException extends Exception {
        private String rawHtml;

        public GoogleTranslateResultNotFoundException(String html) {
            super();
            this.rawHtml = html;
        }

        public String getRawHtml() {
            return rawHtml;
        }
    }
}
