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

import tw.firemaples.onscreenocr.database.DatabaseManager;
import tw.firemaples.onscreenocr.database.ServiceModel;
import tw.firemaples.onscreenocr.database.TranslateServiceModel;
import tw.firemaples.onscreenocr.utils.GoogleWebViewUtil;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 13/09/2017.
 */

public class GoogleTranslateAsyncTask {
    private static String REGEX_RESULT_MATCHER = "TRANSLATED_TEXT='(.[^\\']*)'";
    private static String DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Mobile Safari/537.36";

    public void startTranslate(String textToTranslate, String targetLanguage, final OnGoogleTranslateTaskCallback callback) {
        TranslateServiceModel translateService = DatabaseManager.getInstance().getTranslateService();
        if (translateService.getCurrent() == TranslateServiceModel.TranslateServiceEnum.google) {
            ServiceModel serviceModel = translateService.getCurrentServiceModel();
            if (serviceModel.regexResultMatcher != null && serviceModel.regexResultMatcher.trim().length() > 0) {
                REGEX_RESULT_MATCHER = serviceModel.regexResultMatcher;
            }
            if (serviceModel.defaultUserAgent != null && serviceModel.defaultUserAgent.trim().length() > 0) {
                DEFAULT_USER_AGENT = serviceModel.defaultUserAgent;
            }
        }

        String lang = Locale.forLanguageTag(targetLanguage).getLanguage();
        if (lang.equals(Locale.CHINESE.getLanguage())) {
            lang += "-" + Locale.getDefault().getCountry();
        }

        String url = GoogleWebViewUtil.getFormattedUrl(textToTranslate, lang);

        Tool.logInfo("GoogleTranslateAsyncTask start loading url:" + url);

        String userAgent = new WebView(Tool.getContext()).getSettings().getUserAgentString();
        Tool.logInfo("UserAgent: " + userAgent);
        if (userAgent == null || userAgent.trim().length() == 0) {
            userAgent = DEFAULT_USER_AGENT;
        }
        AndroidNetworking.get(url).setPriority(Priority.HIGH)
                .addHeaders("user-agent", userAgent)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Pattern pattern = Pattern.compile(REGEX_RESULT_MATCHER);
                        Matcher matcher = pattern.matcher(response);
                        if (matcher.find()) {
                            String translatedText = matcher.group(1);
                            Tool.logInfo("GoogleTranslateAsyncTask: result found:" + translatedText);
                            callback.onTranslated(translatedText);
                        } else {
                            GoogleTranslateResultNotFoundException exception = new GoogleTranslateResultNotFoundException(response);
                            Tool.logError("GoogleTranslateAsyncTask: error: " + Log.getStackTraceString(exception));
                            callback.onError(exception);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Tool.logError("GoogleTranslateAsyncTask: error: " + Log.getStackTraceString(anError));
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
