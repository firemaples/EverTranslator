package tw.firemaples.onscreenocr.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.screencrop.TTSPlayerView;
import tw.firemaples.onscreenocr.ocr.OcrNTranslateState;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.utils.FabricUtil;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.utils.ViewPreparedWaiter;

/**
 * Created by firemaples on 29/11/2016.
 */

public class OcrResultWindow {
    private static final Logger logger = LoggerFactory.getLogger(OcrResultWindow.class);

    private final static int MARGIN = 10;

    private final Context context;
    private ViewGroup parent;
    private View anchorView;

    private View rootView;
    private View view_translatedTextWrapper;
    private View pb_origin, pb_translated;
    private View bt_openInBrowser_ocrText, bt_copy_ocrText, bt_edit_ocrText, bt_tts_ocrText, bt_openGoogleTranslate_ocrText;
    private View bt_openInBrowser_translatedText, bt_copy_translatedText, bt_tts_translatedText, bt_openGoogleTranslate_translatedText;
    private TextView tv_originText, tv_translatedText;
    private RelativeLayout.LayoutParams layoutParams;
    private DisplayMetrics metrics;

    private OnOcrResultWindowCallback callback;

    private OcrNTranslateState state;
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

        bt_tts_ocrText = rootView.findViewById(R.id.bt_tts_ocrText);
        bt_edit_ocrText = rootView.findViewById(R.id.bt_edit_ocrText);
        bt_copy_ocrText = rootView.findViewById(R.id.bt_copy_ocrText);
        bt_openInBrowser_ocrText = rootView.findViewById(R.id.bt_openInBrowser_ocrText);
        bt_openGoogleTranslate_ocrText = rootView.findViewById(R.id.bt_openGoogleTranslate_ocrText);

        bt_tts_translatedText = rootView.findViewById(R.id.bt_tts_translatedText);
        bt_copy_translatedText = rootView.findViewById(R.id.bt_copy_translatedText);
        bt_openInBrowser_translatedText = rootView.findViewById(R.id.bt_openInBrowser_translatedText);
        bt_openGoogleTranslate_translatedText = rootView.findViewById(R.id.bt_openGoogleTranslate_translatedText);

        bt_tts_ocrText.setOnClickListener(onClickListener);
        bt_edit_ocrText.setOnClickListener(onClickListener);
        bt_copy_ocrText.setOnClickListener(onClickListener);
        bt_openInBrowser_ocrText.setOnClickListener(onClickListener);
        bt_openGoogleTranslate_ocrText.setOnClickListener(onClickListener);

        bt_copy_translatedText.setOnClickListener(onClickListener);
        bt_openInBrowser_translatedText.setOnClickListener(onClickListener);
        bt_tts_translatedText.setOnClickListener(onClickListener);
        bt_openGoogleTranslate_translatedText.setOnClickListener(onClickListener);

        view_translatedTextWrapper.setVisibility(SharePreferenceUtil.getInstance().isEnableTranslation() ? View.VISIBLE : View.GONE);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);
    }

    public void setOcrResult(OcrNTranslateState state, OcrResult ocrResult) {
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

        boolean ocrFinished = state.getStep() >= OcrNTranslateState.OCR_FINISHED.getStep();
        boolean translated = state.getStep() >= OcrNTranslateState.TRANSLATED.getStep();

        pb_origin.setVisibility(!ocrFinished ? View.VISIBLE : View.GONE);
        pb_translated.setVisibility(ocrFinished && !translated ? View.VISIBLE : View.GONE);
        if (ocrFinished) {
            tv_originText.setText(ocrResult.getText());
        }
        if (translated) {
            tv_translatedText.setText(ocrResult.getTranslatedText());
        }

        bt_tts_ocrText.setEnabled(ocrFinished);
        bt_edit_ocrText.setEnabled(ocrFinished);
        bt_copy_ocrText.setEnabled(ocrFinished);
        bt_openInBrowser_ocrText.setEnabled(ocrFinished);
        bt_openGoogleTranslate_ocrText.setEnabled(ocrFinished);

        bt_tts_translatedText.setEnabled(translated);
        bt_copy_translatedText.setEnabled(translated);
        bt_openInBrowser_translatedText.setEnabled(translated);
        bt_openGoogleTranslate_translatedText.setEnabled(translated);
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

        int parentHeight = parent.getHeight();

        int needHeight = height + MARGIN * 2;

        if (anchorView.getTop() > needHeight) {
            //Gravity = TOP
            layoutParams.topMargin = anchorView.getTop() - height;
        } else if (parentHeight - anchorView.getBottom() > needHeight) {
            //Gravity = BOTTOM
            layoutParams.topMargin = anchorView.getTop() + anchorView.getHeight();
        } else {
            layoutParams.topMargin = MARGIN;
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
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
            logger.info("Width:" + viewToWait.getWidth() + " Height:" + viewToWait.getHeight());
            adjustViewPosition();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_openInBrowser_ocrText) {
                FabricUtil.logBtnOpenInWebViewClicked("OCR text");
                if (ocrResult != null && ocrResult.getText() != null) {
                    callback.onOpenBrowserBtnClick(ocrResult.getText(), false);
                }
            } else if (id == R.id.bt_openInBrowser_translatedText) {
                FabricUtil.logBtnOpenInWebViewClicked("Translated text");
                if (ocrResult != null && ocrResult.getTranslatedText() != null) {
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
            } else if (id == R.id.bt_tts_ocrText || id == R.id.bt_tts_translatedText) {
                String lang;
                String ttsContent;
                if (id == R.id.bt_tts_ocrText) {
                    FabricUtil.logBtnPlayTTSClicked("OCR text");
                    lang = OcrNTranslateUtils.getInstance().getTranslateFromLang();
                    ttsContent = ocrResult.getText();
                } else {
                    FabricUtil.logBtnPlayTTSClicked("Translated text");
                    lang = OcrNTranslateUtils.getInstance().getTranslateToLang();
                    ttsContent = ocrResult.getTranslatedText();
                }

                TTSPlayerView ttsPlayerView = new TTSPlayerView(context);
                ttsPlayerView.setTTSContent(lang, ttsContent);
                ttsPlayerView.attachToWindow();
            } else if (id == R.id.bt_openGoogleTranslate_ocrText) {
                if (ocrResult != null && ocrResult.getText() != null) {
                    callback.openGoogleTranslate(ocrResult.getText(), false);
                }
            } else if (id == R.id.bt_openGoogleTranslate_translatedText) {
                if (ocrResult != null && ocrResult.getTranslatedText() != null) {
                    callback.openGoogleTranslate(ocrResult.getTranslatedText(), true);
                }
            }
        }
    };

    private void copyToClipboard(String label, String text) {
        FabricUtil.logBtnCopyToClipboardClicked(label);
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clipData);
        Tool.getInstance().showMsg(String.format(Locale.getDefault(), context.getString(R.string.msg_textHasBeenCopied), text));
    }

    public interface OnOcrResultWindowCallback {
        void onOpenBrowserBtnClick(String text, boolean translated);

        void onEditOriTextClicked(OcrResult ocrResult);

        void openGoogleTranslate(String text, boolean translated);
    }
}
