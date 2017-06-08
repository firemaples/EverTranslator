package tw.firemaples.onscreenocr.translate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

import tw.firemaples.onscreenocr.BuildConfig;
import tw.firemaples.onscreenocr.utils.FabricUtil;
import tw.firemaples.onscreenocr.utils.GoogleWebViewUtil;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class GoogleTranslateWebView {
    private static final long TIMEOUT = 5000;

    private WebView webView;
    private OnGoogleTranslateWebViewCallback callback;

    @SuppressLint("SetJavaScriptEnabled")
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

        Tool.logInfo("Google translate WebView start loading url:" + url);
        webView.loadUrl(url);
    }

    private class MyWebViewClient extends WebViewClient {
        private boolean timeout = false;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            timeout = true;
            new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (timeout) {
                        Tool.logError("Google translate timeout");
                        FabricUtil.postOnGoogleTranslateTimeout(TIMEOUT);
                        if (callback != null) {
                            callback.onTimeout();
                        }
                    }
                }
            };
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            timeout = false;
            if (BuildConfig.DEBUG) {
                view.loadUrl("javascript:window.HtmlViewer.getHtmlFullContent" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
            view.loadUrl("javascript:window.HtmlViewer.getBodyContent" +
                    "(document.getElementsByTagName('body')[0].innerHTML);");
            view.loadUrl("javascript:window.HtmlViewer.getTranslatedText" +
                    "(document.getElementsByClassName('translation')[0].innerHTML);");
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            FabricUtil.postOnGoogleTranslateFailedException(errorResponse.getStatusCode(), errorResponse.getReasonPhrase());

            if (callback != null) {
                callback.onHttpException(errorResponse.getStatusCode(), errorResponse.getReasonPhrase());
            }
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

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void getHtmlFullContent(String html) {
            Tool.logV("Full html: " + html);
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void getBodyContent(String body) {
            Tool.logV("Body content: " + body);
            if (body == null || body.trim().length() == 0) {
                Tool.logError("postOnGoogleTranslateFailedWithNoneContentException");
                FabricUtil.postOnGoogleTranslateFailedWithNoneContentException();
                if (callback != null) {
                    callback.onNoneException();
                }
            }
        }
    }

    public interface OnGoogleTranslateWebViewCallback {
        void onTranslated(String translatedText);

        void onHttpException(int httpStatus, String reason);

        void onNoneException();

        void onTimeout();
    }
}
