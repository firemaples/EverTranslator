package tw.firemaples.onscreenocr.translate;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;
import java.util.Locale;

import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.utils.WebViewUtil;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class GoogleTranslateWebView {
    private WebView webView;
    private OnGoogleTranslateWebViewCallback callback;

    private List<OcrResult> ocrResultList;

    public GoogleTranslateWebView(Context context) {
        webView = new WebView(context);
        webView.addJavascriptInterface(new MyJavaScriptInterface(context), "HtmlViewer");
        webView.setWebViewClient(new MyWebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
    }

    public void startTranslate(List<OcrResult> ocrResultList, String targetLanguage, OnGoogleTranslateWebViewCallback callback) {
        this.ocrResultList = ocrResultList;
        this.callback = callback;

        if (ocrResultList != null && ocrResultList.size() > 0) {
            String text = ocrResultList.get(0).getText();

            String lang = Locale.forLanguageTag(targetLanguage).getLanguage();
            if (lang.equals(Locale.CHINESE.getLanguage())) {
                lang += "-" + Locale.getDefault().getCountry();
            }

            WebViewUtil.Type type = getServiceType();
            String url = type.getFormattedUrl(text, lang);
            webView.loadUrl(url);
        }
    }

    private WebViewUtil.Type getServiceType() {
        return WebViewUtil.Type.Google;
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
                ocrResultList.get(0).setTranslatedText(text);
                callback.onTranslated(ocrResultList);
            }
        }
    }

    public interface OnGoogleTranslateWebViewCallback {
        void onTranslated(List<OcrResult> ocrResultList);
    }
}
