package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.ScreenTranslatorService;
import tw.firemaples.onscreenocr.captureview.fullscreen.FullScreenCaptureAreaSelectionView;
import tw.firemaples.onscreenocr.ocr.OcrInitAsyncTask;
import tw.firemaples.onscreenocr.ocr.OcrRecognizeAsyncTask;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.translate.TranslateAsyncTask;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 21/10/2016.
 */

public class FloatingBar extends FloatingView {
    private View view_menu, bt_selectArea, bt_translation, bt_clear;
    private Spinner sp_langFrom, sp_langTo;

    private BtnState btnState = BtnState.Normal;
    private AsyncTask<Void, String, Boolean> lastAsyncTask;

    private DrawAreaView drawAreaView;
    private ProgressView progressView;
    private List<Rect> currentBoxList = new ArrayList<>();
    private Bitmap currentScreenshot;

    //Orc
    private OcrNTranslateUtils ocrNTranslateUtils;
    private OcrResultView ocrResultView;
    private OcrInitAsyncTask ocrInitAsyncTask;
    private OcrRecognizeAsyncTask ocrRecognizeAsyncTask;

    //Translation
    private TranslateAsyncTask translateAsyncTask;

    public FloatingBar(Context context) {
        super(context);
        ocrNTranslateUtils = OcrNTranslateUtils.getInstance();
        setViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_floating_bar;
    }

    protected void setViews(View rootView) {
        view_menu = rootView.findViewById(R.id.view_menu);
        bt_selectArea = rootView.findViewById(R.id.bt_selectArea);
        bt_translation = rootView.findViewById(R.id.bt_translation);
        bt_clear = rootView.findViewById(R.id.bt_clear);
        sp_langFrom = (Spinner) rootView.findViewById(R.id.sp_langFrom);
        sp_langTo = (Spinner) rootView.findViewById(R.id.sp_langTo);

        view_menu.setOnTouchListener(onTouchListener);
        view_menu.setOnClickListener(onClickListener);
        bt_selectArea.setOnClickListener(onClickListener);
        bt_translation.setOnClickListener(onClickListener);
        bt_clear.setOnClickListener(onClickListener);

        syncBtnState(BtnState.Normal);

        progressView = new ProgressView(getContext());
        progressView.setCallback(onProgressViewCallback);

        sp_langFrom.setSelection(ocrNTranslateUtils.getOcrLangIndex());
        sp_langTo.setSelection(ocrNTranslateUtils.getTranslateToIndex());
        sp_langFrom.setOnItemSelectedListener(onItemSelectedListener);
        sp_langTo.setOnItemSelectedListener(onItemSelectedListener);
    }

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int parentId = parent.getId();
            if (parentId == R.id.sp_langFrom) {
                String lang = ocrNTranslateUtils.getOcrLangList().get(position);
                ocrNTranslateUtils.setOcrLang(lang);
            } else if (parentId == R.id.sp_langTo) {
                String lang = ocrNTranslateUtils.getTranslateLangList().get(position);
                ocrNTranslateUtils.setTranslateTo(lang);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private ProgressView.OnProgressViewCallback onProgressViewCallback = new ProgressView.OnProgressViewCallback() {
        @Override
        public void onProgressViewAttachedToWindow() {
            FloatingBar.this.detachFromWindow();
            FloatingBar.this.attachToWindow();
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        private int initX, initY;
        private float initTouchX, initTouchY;
        public boolean hasMoved = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    hasMoved = false;
                    initX = getFloatingLayoutParams().x;
                    initY = getFloatingLayoutParams().y;
                    initTouchX = event.getRawX();
                    initTouchY = event.getRawY();
//                    Tool.logInfo("Action down: initX" + initX + " initY:" + initY + " initTouchX:" + initTouchX + " initTouchY:" + initTouchY);
                    break;
                case MotionEvent.ACTION_UP:
                    if (hasMoved) {
                        return true;
                    }
                    hasMoved = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(initTouchX - event.getRawX()) > 20 || Math.abs(initTouchY - event.getRawY()) > 20) {
                        hasMoved = true;
                    }
                    int nextX = initX + (int) (event.getRawX() - initTouchX);
                    int nextY = initY + (int) (event.getRawY() - initTouchY);
                    if (nextX < 0) {
                        nextX = 0;
                    }
                    if (nextY < 0) {
                        nextY = 0;
                    }
                    getFloatingLayoutParams().x = nextX;
                    getFloatingLayoutParams().y = nextY;
                    getWindowManager().updateViewLayout(getRootView(), getFloatingLayoutParams());
//                    Tool.logInfo("Touch location: x:" + event.getRawX() + " y:" + event.getRawY());
//                    Tool.logInfo("New location: x:" + nextX + " y:" + nextY);
                    break;
            }
            return false;
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Tool.logInfo("onClick");
            int id = v.getId();
            if (id == R.id.view_menu) {
                new FloatingBarMenu(getContext(), view_menu, onFloatingBarMenuCallback).show();
            } else if (id == R.id.bt_selectArea) {
                drawAreaView = new DrawAreaView(getContext());
                drawAreaView.attachToWindow();
                FloatingBar.this.detachFromWindow();
                FloatingBar.this.attachToWindow();
                syncBtnState(BtnState.AreaSelecting);
                drawAreaView.getAreaSelectionView().setCallback(onAreaSelectionViewCallback);
            } else if (id == R.id.bt_translation) {
                currentBoxList.addAll(drawAreaView.getAreaSelectionView().getBoxList());
                drawAreaView.getAreaSelectionView().clear();
                drawAreaView.detachFromWindow();
                drawAreaView = null;

                if (takeScreenshot()) {
                    syncBtnState(BtnState.Translating);
                }
            } else if (id == R.id.bt_clear) {
                resetAll();
            }
        }
    };

    private FloatingBarMenu.OnFloatingBarMenuCallback onFloatingBarMenuCallback = new FloatingBarMenu.OnFloatingBarMenuCallback() {
        @Override
        public void onCloseItemClick() {
            resetAll();

            progressView = null;

            ScreenTranslatorService.stop();
        }
    };

    private void resetAll() {
        if (lastAsyncTask != null && !lastAsyncTask.isCancelled()) {
            lastAsyncTask.cancel(true);
        }
        if (ocrInitAsyncTask != null && !ocrInitAsyncTask.isCancelled()) {
            ocrInitAsyncTask.cancel(true);
        }
        if (ocrRecognizeAsyncTask != null && !ocrRecognizeAsyncTask.isCancelled()) {
            ocrRecognizeAsyncTask.cancel(true);
        }
        if (translateAsyncTask != null && !translateAsyncTask.isCancelled()) {
            translateAsyncTask.cancel(true);
        }

        if (drawAreaView != null) {
            drawAreaView.getAreaSelectionView().clear();
            drawAreaView.detachFromWindow();
            drawAreaView = null;
        }

        if (progressView != null) {
            progressView.detachFromWindow();
        }

        if (ocrResultView != null) {
            ocrResultView.clear();
            ocrResultView.detachFromWindow();
            ocrResultView = null;
        }

        currentBoxList.clear();
        currentScreenshot = null;
        syncBtnState(BtnState.Normal);
    }

    private FullScreenCaptureAreaSelectionView.OnAreaSelectionViewCallback onAreaSelectionViewCallback = new FullScreenCaptureAreaSelectionView.OnAreaSelectionViewCallback() {
        @Override
        public void onAreaSelected(FullScreenCaptureAreaSelectionView areaSelectionView) {
            syncBtnState(BtnState.AreaSelected);
        }
    };

    private boolean takeScreenshot() {
        final ScreenshotHandler screenshotHandler = ScreenshotHandler.getInstance();
        if (screenshotHandler.isGetUserPermission()) {
            screenshotHandler.setCallback(onScreenshotHandlerCallback);

            FloatingBar.this.detachFromWindow();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    screenshotHandler.takeScreenshot();
                }
            }, 100);
            return true;
        } else {
            screenshotHandler.getUserPermission();
        }
        return false;
    }

    private ScreenshotHandler.OnScreenshotHandlerCallback onScreenshotHandlerCallback = new ScreenshotHandler.OnScreenshotHandlerCallback() {
        @Override
        public void onScreenshotFinished(Bitmap bitmap) {
            currentScreenshot = bitmap;
            FloatingBar.this.attachToWindow();
            initOrcEngine();
        }
    };

    public void initOrcEngine() {
        if (progressView != null) {
            progressView.showMessage("Waiting for Orc engine initialize...");
        }

        lastAsyncTask = new OcrInitAsyncTask(getContext(), onOrcInitAsyncTaskCallback).execute();
    }

    private OcrInitAsyncTask.OnOrcInitAsyncTaskCallback onOrcInitAsyncTaskCallback =
            new OcrInitAsyncTask.OnOrcInitAsyncTaskCallback() {
                @Override
                public void onOrcInitialized() {
                    showMessage("Orc engine initialized.");
                    startTextRecognize();
                }

                @Override
                public void showMessage(String message) {
                    if (progressView != null) {
                        progressView.showMessage(message);
                    }
                }

                @Override
                public void hideMessage() {
                    if (progressView != null) {
                        progressView.detachFromWindow();
                    }
                }
            };

    public void startTextRecognize() {
        if (progressView != null) {
            progressView.showMessage("Start text recognition...");
        }
        ocrRecognizeAsyncTask = new OcrRecognizeAsyncTask(getContext(), currentScreenshot, currentBoxList, onTextRecognizeAsyncTaskCallback);
        ocrRecognizeAsyncTask.execute();
    }

    private OcrRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback onTextRecognizeAsyncTaskCallback = new OcrRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback() {
        @Override
        public void onTextRecognizeFinished(List<OcrResult> results) {
            startTranslate(results);
        }

        @Override
        public void showMessage(String message) {
            if (progressView != null) {
                progressView.showMessage(message);
            }
        }

        @Override
        public void hideMessage() {
            if (progressView != null) {
                progressView.detachFromWindow();
            }
        }
    };

    public void startTranslate(List<OcrResult> results) {
        translateAsyncTask = new TranslateAsyncTask(getContext(), results, onTranslateAsyncTaskCallback);
        translateAsyncTask.execute();
    }

    private TranslateAsyncTask.OnTranslateAsyncTaskCallback onTranslateAsyncTaskCallback =
            new TranslateAsyncTask.OnTranslateAsyncTaskCallback() {
                @Override
                public void onTranslateFinished(List<OcrResult> translatedResult) {
                    ocrResultView = new OcrResultView(getContext());
                    ocrResultView.attachToWindow();
                    ocrResultView.setOcrResults(translatedResult);
                    FloatingBar.this.detachFromWindow();
                    FloatingBar.this.attachToWindow();
                }

                @Override
                public void showMessage(String message) {
                    if (progressView != null) {
                        progressView.showMessage(message);
                    }
                }

                @Override
                public void hideMessage() {
                    if (progressView != null) {
                        progressView.detachFromWindow();
                    }
                }
            };

    private void syncBtnState(BtnState btnState) {
        this.btnState = btnState;
        switch (btnState) {
            case Normal:
                bt_selectArea.setEnabled(true);
                bt_translation.setEnabled(false);
                bt_clear.setEnabled(false);
                break;
            case AreaSelecting:
                bt_selectArea.setEnabled(false);
                bt_translation.setEnabled(false);
                bt_clear.setEnabled(true);
                break;
            case AreaSelected:
                bt_selectArea.setEnabled(false);
                bt_translation.setEnabled(true);
                bt_clear.setEnabled(true);
                break;
            case Translating:
                bt_selectArea.setEnabled(false);
                bt_translation.setEnabled(false);
                bt_clear.setEnabled(true);
                break;
            case Translated:
                bt_selectArea.setEnabled(true);
                bt_translation.setEnabled(false);
                bt_clear.setEnabled(true);
                break;
        }
    }

    private enum BtnState {
        Normal, AreaSelecting, AreaSelected, Translating, Translated
    }
}
