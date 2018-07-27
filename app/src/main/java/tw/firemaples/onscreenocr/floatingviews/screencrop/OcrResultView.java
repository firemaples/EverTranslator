package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.ocr.OCRManager;
import tw.firemaples.onscreenocr.ocr.OcrNTranslateState;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.translate.GoogleTranslateUtil;
import tw.firemaples.onscreenocr.translate.TranslateManager;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.views.OcrResultWindow;
import tw.firemaples.onscreenocr.views.OcrResultWrapper;

/**
 * Created by firemaples on 01/11/2016.
 */

public class OcrResultView extends FloatingView {
    private static final Logger logger = LoggerFactory.getLogger(OcrResultView.class);

    private OcrResultWrapper view_ocrResultWrapper;
    private WebViewFV webViewFV;
    private TextEditDialogView textEditDialogView;

    private OnOcrResultViewCallback callback;

    private OcrNTranslateState state = OcrNTranslateState.OCR_INIT;

    private List<OcrResult> ocrResultList = new ArrayList<>();

    private OCRManager ocrManager;
    private TranslateManager translateManager;

    public OcrResultView(Context context, OnOcrResultViewCallback callback) {
        super(context);
        this.callback = callback;
        setViews(getRootView());

        ocrManager = OCRManager.getInstance(context);
        ocrManager.setListener(onOCRStateChangedListener);
        translateManager = TranslateManager.getInstance();
    }

    @Override
    protected boolean layoutFocusable() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_result_view;
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected boolean fullScreenMode() {
        return true;
    }

    private void setViews(View rootView) {
        webViewFV = new WebViewFV(getContext(), onWebViewFVCallback);

        view_ocrResultWrapper = new OcrResultWrapper(getContext(), onOcrResultWindowCallback);
        ((ViewGroup) rootView).addView(view_ocrResultWrapper, 0, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setupData(Bitmap screenshot, List<Rect> boxList) {
        this.ocrResultList = new ArrayList<>();

        for (Rect rect : boxList) {
            OcrResult ocrResult = new OcrResult();
            ocrResult.setRect(rect);
            ArrayList<Rect> rects = new ArrayList<>();
            rects.add(new Rect(0, 0, rect.width(), rect.height()));
            ocrResult.setBoxRects(rects);
            ocrResultList.add(ocrResult);
        }

        ocrManager.start(screenshot, boxList);
    }

    private void setDebugInfo(OcrResult ocrResult) {
        TextView tv_debugInfo = (TextView) getRootView().findViewById(R.id.tv_debugInfo);
        if (ocrResult.getDebugInfo() == null) {
            tv_debugInfo.setVisibility(View.GONE);
            return;
        }
        String[] infoArray = ocrResult.getDebugInfo().getInfoList().toArray(new String[ocrResult.getDebugInfo().getInfoList().size()]);
        String debugInfoString = TextUtils.join("\n", infoArray);
        tv_debugInfo.setText(debugInfoString);
        tv_debugInfo.setVisibility(View.VISIBLE);
    }

    private void updateViewState(OcrNTranslateState newState) {
        this.state = newState;
        updateViewState();
    }

    private void updateViewState() {
        if (SharePreferenceUtil.getInstance().isDebugMode()) {
            if (ocrResultList.size() > 0) {
                setDebugInfo(ocrResultList.get(0));
            }
        }

        view_ocrResultWrapper.updateViewState(state, ocrResultList);
    }


    public void clear() {
        ocrManager.cancelRunningTask();

        view_ocrResultWrapper.clear();
        if (textEditDialogView != null) {
            textEditDialogView.detachFromWindow();
        }

        if (webViewFV != null) {
            webViewFV.detachFromWindow();
        }
    }

    @Override
    public void detachFromWindow() {
        clear();
        super.detachFromWindow();
    }

    private OCRManager.OnOCRStateChangedListener onOCRStateChangedListener = new OCRManager.OnOCRStateChangedListener() {
        @Override
        public void onInitializing() {
            updateViewState(OcrNTranslateState.OCR_INIT);
        }

        @Override
        public void onInitialized() {

        }

        @Override
        public void onRecognizing() {
            updateViewState(OcrNTranslateState.OCR_RUNNING);
        }

        @Override
        public void onRecognized(List<OcrResult> results) {
            OcrResultView.this.ocrResultList.clear();
            OcrResultView.this.ocrResultList.addAll(results);
            updateViewState(state = OcrNTranslateState.OCR_FINISHED);
            startTranslate(results);
        }
    };

    private void startTranslate(List<OcrResult> results) {
        if (SharePreferenceUtil.getInstance().isEnableTranslation() && results.size() > 0) {
            updateViewState(OcrNTranslateState.TRANSLATING);

            translateManager.startTranslate(getContext(), results.get(0).getText(), onTranslateManagerCallback);

        } else {
            for (OcrResult result : results) {
                result.setTranslatedText("");
            }
            onTranslateManagerCallback.onTranslateFinished("");
        }
    }

    private TranslateManager.OnTranslateManagerCallback onTranslateManagerCallback = new TranslateManager.OnTranslateManagerCallback() {
        @Override
        public void onTranslateFinished(String translatedText) {
            if (ocrResultList.size() > 0) {
                ocrResultList.get(0).setTranslatedText(translatedText);
            }
            updateViewState(OcrNTranslateState.TRANSLATED);
        }
    };

    private OcrResultWindow.OnOcrResultWindowCallback onOcrResultWindowCallback = new OcrResultWindow.OnOcrResultWindowCallback() {
        @Override
        public void onOpenBrowserBtnClick(String text, boolean translated) {
            String lang;
            if (translated) {
                lang = OcrNTranslateUtils.getInstance().getTranslateFromLang();
            } else {
                lang = OcrNTranslateUtils.getInstance().getTranslateToLang();
            }
            webViewFV.setContent(text, lang);
            webViewFV.attachToWindow();
        }

        @Override
        public void onEditOriTextClicked(OcrResult ocrResult) {
            logger.info("onEditOriTextClicked: " + ocrResult.getText());
            textEditDialogView = new TextEditDialogView(getContext());
            textEditDialogView.setCallback(onTextEditDialogViewCallback);
            textEditDialogView.setTitle(getContext().getString(R.string.title_editOCRText));
            textEditDialogView.setContentText(ocrResult.getText());
            textEditDialogView.setTag(ocrResult);
            textEditDialogView.attachToWindow();
        }

        @Override
        public void openGoogleTranslate(String text, boolean translated) {
            String lang;
            if (translated) {
                lang = OcrNTranslateUtils.getInstance().getTranslateFromLang();
            } else {
                lang = OcrNTranslateUtils.getInstance().getTranslateToLang();
            }

            if (GoogleTranslateUtil.start(getContext(), lang, text)) {
                callback.onOpenGoogleTranslateClicked();
            }
        }
    };

    private TextEditDialogView.OnTextEditDialogViewCallback onTextEditDialogViewCallback =
            new TextEditDialogView.OnTextEditDialogViewCallback() {
                @Override
                public void OnConfirmClick(TextEditDialogView textEditDialogView, String text) {
                    super.OnConfirmClick(textEditDialogView, text);
                    OcrResult ocrResult = (OcrResult) textEditDialogView.getTag();
                    if (ocrResult != null) {
                        if (!ocrResult.getText().trim().equals(text.trim())) {
                            ocrResult.setText(text);
                            ocrResult.setTranslatedText("");
                            List<OcrResult> cloneList = new ArrayList<>();
                            cloneList.addAll(ocrResultList);
                            startTranslate(cloneList);
                        }
                    }
                }
            };

    private WebViewFV.OnWebViewFVCallback onWebViewFVCallback = new WebViewFV.OnWebViewFVCallback() {
        @Override
        public void onOpenBrowserClicked() {
            webViewFV.detachFromWindow();
            OcrResultView.this.clear();
            OcrResultView.this.detachFromWindow();

            callback.onOpenBrowserClicked();
        }
    };

    public interface OnOcrResultViewCallback {
        void onOpenBrowserClicked();

        void onOpenGoogleTranslateClicked();
    }
}
