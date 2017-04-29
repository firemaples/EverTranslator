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
import tw.firemaples.onscreenocr.floatingviews.screencrop.OcrResultView;
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
    private View pb_origin, pb_translated;
    private View bt_openInBrowser_ocrText, bt_openInBrowser_translatedText, bt_copy_ocrText, bt_copy_translatedText, bt_edit_ocrText;
    private TextView tv_originText, tv_translatedText;
    private FrameLayout.LayoutParams layoutParams;
    private DisplayMetrics metrics;

    private OnOcrResultWindowCallback callback;

    private OcrResultView.OcrNTranslateState state;
    private OcrResult ocrResult;

    public OcrResultWindow(Context context, ViewGroup parent, OnOcrResultWindowCallback callback) {
        this.context = context;
        this.parent = parent;
        this.callback = callback;

        rootView = View.inflate(context, R.layout.view_ocr_result_window, null);

        view_translatedTextWrapper = rootView.findViewById(R.id.view_translatedTextWrapper);
        pb_origin = rootView.findViewById(R.id.pb_origin);
        pb_translated = rootView.findViewById(R.id.pb_translated);
        tv_originText = (TextView) rootView.findViewById(R.id.tv_originText);
        tv_translatedText = (TextView) rootView.findViewById(R.id.tv_translatedText);
        bt_openInBrowser_ocrText = rootView.findViewById(R.id.bt_openInBrowser_ocrText);
        bt_openInBrowser_translatedText = rootView.findViewById(R.id.bt_openInBrowser_translatedText);
        bt_copy_ocrText = rootView.findViewById(R.id.bt_copy_ocrText);
        bt_copy_translatedText = rootView.findViewById(R.id.bt_copy_translatedText);
        bt_edit_ocrText = rootView.findViewById(R.id.bt_edit_ocrText);
        bt_edit_ocrText.setOnClickListener(onClickListener);
        bt_copy_ocrText.setOnClickListener(onClickListener);
        bt_openInBrowser_ocrText.setOnClickListener(onClickListener);
        bt_copy_translatedText.setOnClickListener(onClickListener);
        bt_openInBrowser_translatedText.setOnClickListener(onClickListener);

        view_translatedTextWrapper.setVisibility(SharePreferenceUtil.getInstance().isEnableTranslation() ? View.VISIBLE : View.GONE);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);
    }

    public void setOcrResult(OcrResultView.OcrNTranslateState state, OcrResult ocrResult) {
        this.state = state;
        this.ocrResult = ocrResult;

//        switch (state) {
//            case OCR_INIT:
//            case OCR_RUNNING:
//                pb_origin.setVisibility(View.VISIBLE);
//                pb_translated.setVisibility(View.GONE);
//                break;
//            case OCR_FINISHED:
//                pb_origin.setVisibility(View.GONE);
//                tv_originText.setText(ocrResult.getText());
//                break;
//            case TRANSLATING:
//                pb_translated.setVisibility(View.VISIBLE);
//                tv_originText.setText(ocrResult.getText());
//                break;
//            case TRANSLATED:
//                pb_translated.setVisibility(View.GONE);
//                tv_originText.setText(ocrResult.getText());
//                tv_translatedText.setText(ocrResult.getTranslatedText());
//                break;
//        }

        boolean ocrFinished = state.getStep() >= OcrResultView.OcrNTranslateState.OCR_FINISHED.getStep();
        boolean translated = state.getStep() >= OcrResultView.OcrNTranslateState.TRANSLATED.getStep();

        pb_origin.setVisibility(!ocrFinished ? View.VISIBLE : View.GONE);
        pb_translated.setVisibility(ocrFinished && !translated ? View.VISIBLE : View.GONE);
        if (ocrFinished) {
            tv_originText.setText(ocrResult.getText());
        }
        if (translated) {
            tv_translatedText.setText(ocrResult.getTranslatedText());
        }

        bt_edit_ocrText.setEnabled(ocrFinished);
        bt_copy_ocrText.setEnabled(ocrFinished);
        bt_openInBrowser_ocrText.setEnabled(ocrFinished);
        bt_copy_translatedText.setEnabled(translated);
        bt_openInBrowser_translatedText.setEnabled(translated);
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
            } else if (id == R.id.bt_edit_ocrText) {
                if (ocrResult != null) {
                    callback.onEditOriTextClicked(ocrResult);
                }
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

        void onEditOriTextClicked(OcrResult ocrResult);
    }
}
