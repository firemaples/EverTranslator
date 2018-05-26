package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.database.ServiceHolderModel;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.utils.FabricUtil;
import tw.firemaples.onscreenocr.utils.HomeWatcher;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.utils.UrlFormatter;

/**
 * Created by firemaples on 01/12/2016.
 */

public class WebViewFV extends FloatingView {
    private static final Logger logger = LoggerFactory.getLogger(WebViewFV.class);

    private WebView wv_webView;
    private String url;
    private OnWebViewFVCallback callback;

    public WebViewFV(Context context, OnWebViewFVCallback callback) {
        super(context);
        this.callback = callback;
        initViews(getRootView());
    }

    @Override
    protected boolean layoutFocusable() {
        return true;
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

        setupHomeButtonWatcher(onHomePressedListener);
    }

    @Override
    public boolean onBackButtonPressed() {
        detachFromWindow();
        return true;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_openBrowser) {
                FabricUtil.logBtnOpenOnOtherBrowserClicked();
                Tool.getInstance().openBrowser(url);
                callback.onOpenBrowserClicked();
            } else if (id == R.id.bt_close) {
                detachFromWindow();
            }
        }
    };

    private HomeWatcher.OnHomePressedListener onHomePressedListener = new HomeWatcher.OnHomePressedListener() {
        @Override
        public void onHomePressed() {
            WebViewFV.this.detachFromWindow();
        }

        @Override
        public void onHomeLongPressed() {

        }
    };

    public void setContent(String text, String targetLanguage) {
        url = UrlFormatter.getFormattedUrl(ServiceHolderModel.SERVICE_GOOGLE_WEB, text, targetLanguage);
        logger.info("Start loading google web: " + url);
        wv_webView.loadUrl(url);
    }

    public interface OnWebViewFVCallback {
        void onOpenBrowserClicked();
    }
}
