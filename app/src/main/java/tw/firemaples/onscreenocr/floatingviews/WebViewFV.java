package tw.firemaples.onscreenocr.floatingviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.utils.WebViewService;

/**
 * Created by louis1chen on 01/12/2016.
 */

public class WebViewFV extends FloatingView {
    private WebView wv_webView;
    private String url;
    private OnWebViewFVCallback callback;

    public WebViewFV(Context context, OnWebViewFVCallback callback) {
        super(context);
        this.callback = callback;
        initViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_web_view;
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initViews(View rootView) {
        rootView.findViewById(R.id.bt_openBrowser).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.bt_close).setOnClickListener(onClickListener);

        wv_webView = (WebView) rootView.findViewById(R.id.wv_webView);
        wv_webView.setWebViewClient(new WebViewClient());
        WebSettings settings = wv_webView.getSettings();
        settings.setJavaScriptEnabled(true);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_openBrowser) {
                Tool.getInstance().openBrowser(url);
                callback.onOpenBrowserClicked();
            } else if (id == R.id.bt_close) {
                detachFromWindow();
            }
        }
    };

    public void setContent(String text) {
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals(Locale.CHINESE.getLanguage())) {
            lang += "-" + Locale.getDefault().getCountry();
        }
        setContent(text, lang);
    }

    public void setContent(String text, String targetLanguage) {
        WebViewService.Type type = getServiceType();
        url = type.getFormattedUrl(text, targetLanguage);
        wv_webView.loadUrl(url);
    }

    private WebViewService.Type getServiceType() {
        return WebViewService.Type.Google;
    }

    public interface OnWebViewFVCallback {
        void onOpenBrowserClicked();
    }
}
