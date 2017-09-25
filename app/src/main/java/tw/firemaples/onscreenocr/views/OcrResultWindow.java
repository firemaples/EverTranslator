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

package tw.firemaples.onscreenocr.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.utils.ViewPreparedWaiter;

/**
 * Created by firemaples on 29/11/2016.
 */

public class OcrResultWindow {
    private final static int MARGIN = 10;

    private final Context context;
    private ViewGroup parent;
    private View anchorView;

    private View rootView;
    private View view_translatedTextWrapper;
    private TextView tv_originText, tv_translatedText;
    private FrameLayout.LayoutParams layoutParams;
    private DisplayMetrics metrics;

    private OnOcrResultWindowCallback callback;

    private OcrResult ocrResult;

    public OcrResultWindow(Context context, ViewGroup parent, OnOcrResultWindowCallback callback) {
        this.context = context;
        this.parent = parent;
        this.callback = callback;

        rootView = View.inflate(context, R.layout.view_ocr_result_window, null);

        view_translatedTextWrapper = rootView.findViewById(R.id.view_translatedTextWrapper);
        tv_originText = (TextView) rootView.findViewById(R.id.tv_originText);
        tv_translatedText = (TextView) rootView.findViewById(R.id.tv_translatedText);
        rootView.findViewById(R.id.bt_openInBrowser_ocrText).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.bt_openInBrowser_translatedText).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.bt_copy_ocrText).setOnClickListener(onClickListener);
        rootView.findViewById(R.id.bt_copy_translatedText).setOnClickListener(onClickListener);

        view_translatedTextWrapper.setVisibility(SharePreferenceUtil.getInstance().isEnableTranslation() ? View.VISIBLE : View.GONE);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);
    }

    public void setOcrResult(OcrResult ocrResult) {
        this.ocrResult = ocrResult;
        tv_originText.setText(ocrResult.getText());
        tv_translatedText.setText(ocrResult.getTranslatedText());
    }

    public void show(View anchorView) {
        dismiss();
        this.anchorView = anchorView;

        new ViewPreparedWaiter().waitView(rootView, onViewPrepared);
        parent.addView(rootView, layoutParams);
    }

    public void dismiss() {
        if (rootView.getParent() != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    private void adjustViewPosition() {
        int width = rootView.getWidth();
        int height = rootView.getHeight();

        if (anchorView.getTop() > height + MARGIN * 2) {
            //Gravity = TOP
            layoutParams.topMargin = anchorView.getTop() - height;
        } else {
            //Gravity = BOTTOM
            layoutParams.topMargin = anchorView.getTop() + anchorView.getHeight();
        }

        if (anchorView.getLeft() + width + MARGIN > metrics.widthPixels) {
            // Match screen right
            layoutParams.leftMargin = metrics.widthPixels - (width + MARGIN);
        } else {
            // Match anchorView left
            layoutParams.leftMargin = anchorView.getLeft();
        }

        parent.updateViewLayout(rootView, layoutParams);
    }

    private ViewPreparedWaiter.OnViewPrepared onViewPrepared = new ViewPreparedWaiter.OnViewPrepared() {
        @Override
        public void onViewPrepared(View viewToWait) {
            Tool.logInfo("Width:" + viewToWait.getWidth() + " Height:" + viewToWait.getHeight());
            adjustViewPosition();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_openInBrowser_ocrText) {
                Answers.getInstance().logCustom(new CustomEvent("Btn open in browser").putCustomAttribute("Type", "OCR text"));
                if (ocrResult != null) {
                    callback.onOpenBrowserBtnClick(ocrResult.getText(), false);
                }
            } else if (id == R.id.bt_openInBrowser_translatedText) {
                Answers.getInstance().logCustom(new CustomEvent("Btn open in browser").putCustomAttribute("Type", "Translated text"));
                if (ocrResult != null) {
                    callback.onOpenBrowserBtnClick(ocrResult.getTranslatedText(), true);
                }
            } else if (id == R.id.bt_copy_ocrText) {
                copyToClipboard("OCR text", ocrResult.getText());
            } else if (id == R.id.bt_copy_translatedText) {
                copyToClipboard("Translated text", ocrResult.getTranslatedText());
            }
        }
    };

    private void copyToClipboard(String label, String text) {
        Answers.getInstance().logCustom(new CustomEvent("Copy to Clipboard").putCustomAttribute("Type", label));
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clipData);
        Tool.getInstance().showMsg(String.format(Locale.getDefault(), context.getString(R.string.msg_textHasBeenCopied), text));
    }

    public interface OnOcrResultWindowCallback {
        void onOpenBrowserBtnClick(String text, boolean translated);
    }
}
