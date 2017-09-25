/*
 * Copyright 2016-2017 Louis Chen [firemaples@gmail.com].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.utils.WebViewUtil;

/**
 * Created by firemaples on 01/12/2016.
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
                Answers.getInstance().logCustom(new CustomEvent("Btn open on other browser"));
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
        WebViewUtil.Type type = getServiceType();
        url = type.getFormattedUrl(text, targetLanguage);
        wv_webView.loadUrl(url);
    }

    private WebViewUtil.Type getServiceType() {
        return WebViewUtil.Type.Google;
    }

    public interface OnWebViewFVCallback {
        void onOpenBrowserClicked();
    }
}
