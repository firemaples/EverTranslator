package tw.firemaples.onscreenocr.translate;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.utils.GoogleWebViewUtil;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class GoogleTranslateWebView {
    private WebView webView;
    private OnGoogleTranslateWebViewCallback callback;

    public GoogleTranslateWebView(Context context) {
        webView = new WebView(context);
        webView.addJavascriptInterface(new MyJavaScriptInterface(context), "HtmlViewer");
        webView.setWebViewClient(new MyWebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
    }

    public void startTranslate(String textToTranslate, String targetLanguage, OnGoogleTranslateWebViewCallback callback) {
        this.callback = callback;

        String lang = Locale.forLanguageTag(targetLanguage).getLanguage();
        if (lang.equals(Locale.CHINESE.getLanguage())) {
            lang += "-" + Locale.getDefault().getCountry();
        }

        String url = GoogleWebViewUtil.getFormattedUrl(textToTranslate, lang);
        webView.loadUrl(url);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:window.HtmlViewer.getTranslatedText" +
                    "(document.getElementsByClassName('translation')[0].innerHTML);");
        }
    }

    private class MyJavaScriptInterface {

        @SuppressWarnings("unused")
        private Context context;

        MyJavaScriptInterface(Context context) {
            this.context = context;
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void getTranslatedText(String text) {
            Tool.logInfo("Translated text: " + text);
            if (callback != null) {
                callback.onTranslated(text);
            }
        }
    }

    public interface OnGoogleTranslateWebViewCallback {
        void onTranslated(String translatedText);
    }
}
