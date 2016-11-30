package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.FrameLayout;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.WebViewService;

/**
 * Created by louis1chen on 30/11/2016.
 */

public class WebViewWrapper extends FrameLayout {
    private WebView wv_webView;

    public WebViewWrapper(Context context) {
        super(context);
        initViews();
    }

    public WebViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public WebViewWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public WebViewWrapper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.view_webview, this);
        wv_webView = (WebView) findViewById(R.id.wv_webView);
    }

    public void show(String text, String targetLanguage) {
        WebViewService.Type type = getServiceType();
        String formattedUrl = type.getFormattedUrl(text, targetLanguage);
        wv_webView.loadUrl(formattedUrl);
        this.setVisibility(VISIBLE);
    }

    public void hide() {
        this.setVisibility(GONE);
    }

    private WebViewService.Type getServiceType() {
        return WebViewService.Type.Google;
    }
}
