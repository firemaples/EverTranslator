package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.ocr.OcrInitAsyncTask;
import tw.firemaples.onscreenocr.ocr.OcrRecognizeAsyncTask;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.translate.TranslateManager;
import tw.firemaples.onscreenocr.utils.FabricUtil;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.views.OcrResultWindow;
import tw.firemaples.onscreenocr.views.OcrResultWrapper;

/**
 * Created by firemaples on 01/11/2016.
 */

public class OcrResultView extends FloatingView {
    private OcrResultWrapper view_ocrResultWrapper;
    private WebViewFV webViewFV;
    private TextEditDialogView textEditDialogView;

    private OnOcrResultViewCallback callback;

    private OcrNTranslateState state = OcrNTranslateState.OCR_INIT;

    private AsyncTask lastAsyncTask = null;
    private Bitmap currentScreenshot;
    private List<Rect> boxList;
    private List<OcrResult> ocrResultList = new ArrayList<>();

    private TranslateManager translateManager;

    public OcrResultView(Context context, OnOcrResultViewCallback callback) {
        super(context);
        this.callback = callback;
        setViews(getRootView());
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
        this.currentScreenshot = screenshot;
        this.boxList = boxList;
        this.ocrResultList = new ArrayList<>();

        for (Rect rect : boxList) {
            OcrResult ocrResult = new OcrResult();
            ocrResult.setRect(rect);
            ArrayList<Rect> rects = new ArrayList<>();
            rects.add(new Rect(0, 0, rect.width(), rect.height()));
            ocrResult.setBoxRects(rects);
            ocrResultList.add(ocrResult);
        }

        initOcrEngine();
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
        lastAsyncTask.cancel(true);

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

    private void initOcrEngine() {
        updateViewState(OcrNTranslateState.OCR_INIT);

        lastAsyncTask = new OcrInitAsyncTask(getContext(), onOcrInitAsyncTaskCallback).execute();
    }

    private OcrInitAsyncTask.OnOcrInitAsyncTaskCallback onOcrInitAsyncTaskCallback =
            new OcrInitAsyncTask.OnOcrInitAsyncTaskCallback() {
                @Override
                public void onOcrInitialized() {
//                    showMessage(getContext().getString(R.string.progress_ocrInitialized));
                    startTextRecognize();
                }

                @Override
                public void showMessage(String message) {
                }

                @Override
                public void hideMessage() {
                }
            };

    private void startTextRecognize() {
        updateViewState(OcrNTranslateState.OCR_RUNNING);

        FabricUtil.logStartOCROperation();

        lastAsyncTask = new OcrRecognizeAsyncTask(getContext(),
                currentScreenshot,
                boxList,
                onTextRecognizeAsyncTaskCallback).execute();
    }

    private OcrRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback onTextRecognizeAsyncTaskCallback =
            new OcrRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback() {
                @Override
                public void onTextRecognizeFinished(List<OcrResult> results) {
                    OcrResultView.this.ocrResultList.clear();
                    OcrResultView.this.ocrResultList.addAll(results);
                    updateViewState(state = OcrNTranslateState.OCR_FINISHED);
                    startTranslate(results);
                }

                @Override
                public void showMessage(String message) {
                }

                @Override
                public void hideMessage() {
                }
            };

    private void startTranslate(List<OcrResult> results) {
        if (SharePreferenceUtil.getInstance().isEnableTranslation() && results.size() > 0) {
            updateViewState(OcrNTranslateState.TRANSLATING);
            FabricUtil.logStartTranslateOperation();

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
            FabricUtil.logFinishTranslateOperation();
            if (ocrResultList.size() > 0) {
                ocrResultList.get(0).setTranslatedText(translatedText);
            }
            updateViewState(OcrNTranslateState.TRANSLATED);
        }
    };

    private OcrResultWindow.OnOcrResultWindowCallback onOcrResultWindowCallback = new OcrResultWindow.OnOcrResultWindowCallback() {
        @Override
        public void onOpenBrowserBtnClick(String text, boolean translated) {
            webViewFV.setContent(text);
            webViewFV.attachToWindow();
        }

        @Override
        public void onEditOriTextClicked(OcrResult ocrResult) {
            Tool.logInfo("onEditOriTextClicked: " + ocrResult.getText());
            textEditDialogView = new TextEditDialogView(getContext());
            textEditDialogView.setCallback(onTextEditDialogViewCallback);
            textEditDialogView.setTitle(getContext().getString(R.string.title_editOCRText));
            textEditDialogView.setContentText(ocrResult.getText());
            textEditDialogView.setTag(ocrResult);
            textEditDialogView.attachToWindow();
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

    public enum OcrNTranslateState {
        OCR_INIT(1), OCR_RUNNING(2), OCR_FINISHED(3),
        TRANSLATING(4), TRANSLATED(5);

        private int step;

        OcrNTranslateState(int step) {
            this.step = step;
        }

        public int getStep() {
            return step;
        }
    }

    public interface OnOcrResultViewCallback {
        void onOpenBrowserClicked();
    }
}
