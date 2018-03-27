package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.firemaples.onscreenocr.MainActivity;
import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.ScreenTranslatorService;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.floatingviews.MovableFloatingView;
import tw.firemaples.onscreenocr.ocr.OcrDownloadTask;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.translate.TranslateManager;
import tw.firemaples.onscreenocr.utils.AppMode;
import tw.firemaples.onscreenocr.utils.FabricUtil;
import tw.firemaples.onscreenocr.utils.HomeWatcher;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.views.AreaSelectionView;
import tw.firemaples.onscreenocr.views.FloatingBarMenu;

/**
 * Created by firemaples on 21/10/2016.
 */

public class FloatingBar extends MovableFloatingView {
    private View view_menu, bt_selectArea, bt_translation, bt_clear;
    private Spinner sp_langFrom, sp_langTo;

    private BtnState btnState = BtnState.Normal;
    private AsyncTask<Void, String, Boolean> lastAsyncTask;

    private DialogView dialogView;
    private DrawAreaView drawAreaView;
    private ProgressView progressView;
    private List<Rect> currentBoxList = new ArrayList<>();

    //OCR
    private OcrNTranslateUtils ocrNTranslateUtils;
    private OcrResultView ocrResultView;
    private OcrDownloadTask ocrDownloadTask;

    public FloatingBar(Context context) {
        super(context);
        ocrNTranslateUtils = OcrNTranslateUtils.getInstance();
        setViews(getRootView());
    }

    @Override
    public void attachToWindow() {
        super.attachToWindow();
        SharePreferenceUtil.getInstance().setIsAppShowing(true);

        if (!SharePreferenceUtil.getInstance().isVersionHistoryAlreadyShown(getContext())) {
            new VersionHistoryView(getContext()).attachToWindow();
        }
    }

    private void detachFromWindow(boolean reset) {
        SharePreferenceUtil.getInstance().setIsAppShowing(false);
        if (reset) {
            detachFromWindow();
        } else {
            super.detachFromWindow();
        }
    }

    private OnBackButtonPressedListener subViewOnBackButtonPressedListener = new OnBackButtonPressedListener() {
        @Override
        public boolean onBackButtonPressed(FloatingView floatingView) {
            return FloatingBar.this.onBackButtonPressed();
        }
    };

    private HomeWatcher.OnHomePressedListener subViewOnHomePressedListener = new HomeWatcher.OnHomePressedListener() {
        @Override
        public void onHomePressed() {
            FloatingBar.this.onBackButtonPressed();
        }

        @Override
        public void onHomeLongPressed() {

        }
    };

    @Override
    public boolean onBackButtonPressed() {
        if (btnState != BtnState.Normal) {
            bt_clear.performClick();
            return true;
        }
        return super.onBackButtonPressed();
    }

    @Override
    public void detachFromWindow() {
        SharePreferenceUtil.getInstance().setIsAppShowing(false);
        resetAll();
        super.detachFromWindow();
        ScreenTranslatorService.resetForeground();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_floating_bar;
    }

    @Override
    protected int getLayoutGravity() {
        return Gravity.TOP | Gravity.RIGHT;
    }

    private void setViews(View rootView) {
        view_menu = rootView.findViewById(R.id.view_menu);
        bt_selectArea = rootView.findViewById(R.id.bt_selectArea);
        bt_translation = rootView.findViewById(R.id.bt_translation);
        bt_clear = rootView.findViewById(R.id.bt_clear);
        sp_langFrom = (Spinner) rootView.findViewById(R.id.sp_langFrom);
        sp_langTo = (Spinner) rootView.findViewById(R.id.sp_langTo);

        view_menu.setOnClickListener(onClickListener);
        bt_selectArea.setOnClickListener(onClickListener);
        bt_translation.setOnClickListener(onClickListener);
        bt_clear.setOnClickListener(onClickListener);

        syncBtnState(BtnState.Normal);

        progressView = new ProgressView(getContext());
        progressView.setCallback(onProgressViewCallback);

        sp_langFrom.setSelection(ocrNTranslateUtils.getOcrLangIndex());
        sp_langTo.setSelection(ocrNTranslateUtils.getTranslateToIndex());
        sp_langTo.setEnabled(SharePreferenceUtil.getInstance().isEnableTranslation());
        sp_langFrom.setOnItemSelectedListener(onItemSelectedListener);
        sp_langTo.setOnItemSelectedListener(onItemSelectedListener);

        dialogView = new DialogView(getContext());

        setDragView(view_menu);

        if (SharePreferenceUtil.getInstance().startingWithSelectionMode()) {
            onClickListener.onClick(bt_selectArea);
        }
    }

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int parentId = parent.getId();
            if (parentId == R.id.sp_langFrom) {
                String lang = ocrNTranslateUtils.getOcrLangList().get(position);
                ocrNTranslateUtils.setOcrLang(lang);

                if (!OcrDownloadTask.checkOcrFiles(lang)) {
                    showDownloadOcrFileDialog(OcrNTranslateUtils.getInstance().getOcrLangDisplayName(lang));
                }

            } else if (parentId == R.id.sp_langTo) {
                String lang = ocrNTranslateUtils.getTranslateLangList().get(position);
                ocrNTranslateUtils.setTranslateTo(lang);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void showDownloadOcrFileDialog(String langName) {
        dialogView.reset();
        dialogView.setTitle(getContext().getString(R.string.dialog_title_ocrFileNotFound));
        dialogView.setContentMsg(String.format(Locale.getDefault(), getContext().getString(R.string.dialog_content_ocrFileNotFound), langName));
        dialogView.setType(DialogView.Type.CONFIRM_CANCEL);
        dialogView.getOkBtn().setText(R.string.btn_download);
        dialogView.setCallback(new DialogView.OnDialogViewCallback() {
            @Override
            public void OnConfirmClick(DialogView dialogView) {
                super.OnConfirmClick(dialogView);
                ocrDownloadTask = new OcrDownloadTask(getContext(), onOcrDownloadAsyncTaskCallback);
                ocrDownloadTask.startDownload();
            }
        });
        dialogView.attachToWindow();
    }

    private OcrDownloadTask.OnOcrDownloadAsyncTaskCallback onOcrDownloadAsyncTaskCallback = new OcrDownloadTask.OnOcrDownloadAsyncTaskCallback() {
        @Override
        public void onDownloadStart() {
            dialogView.reset();
            dialogView.setType(DialogView.Type.CANCEL_ONLY);
            dialogView.setTitle(getContext().getString(R.string.dialog_title_ocrFileDownloading));
            dialogView.setContentMsg(getContext().getString(R.string.dialog_content_ocrFileDownloadStarting));
            dialogView.attachToWindow();
            dialogView.setCallback(new DialogView.OnDialogViewCallback() {
                @Override
                public void onCancelClicked(DialogView dialogView) {
                    if (ocrDownloadTask != null) {
                        ocrDownloadTask.cancel();
                    }
                    super.onCancelClicked(dialogView);
                }
            });
        }

        @Override
        public void onDownloadFinished() {
            dialogView.detachFromWindow();
        }

        @Override
        public void downloadProgressing(long currentDownloaded, long totalSize, String msg) {
            dialogView.setContentMsg(msg);
        }

        @Override
        public void onError(final String errorMessage) {
            getRootView().post(new Runnable() {
                @Override
                public void run() {
                    dialogView.setTitle(getContext().getString(R.string.dialog_title_error));
                    dialogView.setContentMsg(errorMessage);
                }
            });
        }
    };

    private ProgressView.OnProgressViewCallback onProgressViewCallback = new ProgressView.OnProgressViewCallback() {
        @Override
        public void onProgressViewAttachedToWindow() {
            FloatingBar.this.detachFromWindow(false);
            FloatingBar.this.attachToWindow();
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
                if (SharePreferenceUtil.getInstance().isDebugMode()) {
                    TranslateManager.getInstance().test(getContext());
                }

                FabricUtil.logBtnSelectAreaClicked();
                if (ScreenshotHandler.isInitialized()) {
                    FabricUtil.logDoBtnSelectAreaAction();
                    drawAreaView = new DrawAreaView(getContext());
                    drawAreaView.setOnBackButtonPressedListener(subViewOnBackButtonPressedListener);
                    drawAreaView.setupHomeButtonWatcher(subViewOnHomePressedListener);
                    drawAreaView.attachToWindow();
                    FloatingBar.this.detachFromWindow(false);
                    FloatingBar.this.attachToWindow();
                    syncBtnState(BtnState.AreaSelecting);
                    drawAreaView.getAreaSelectionView().setCallback(onAreaSelectionViewCallback);
                    if (SharePreferenceUtil.getInstance().isRememberLastSelection()) {
                        drawAreaView.getAreaSelectionView().setBoxList(SharePreferenceUtil.getInstance().getLastSelectionArea());
                    }
                } else {
                    MainActivity.start(getContext());
                    ScreenTranslatorService.stop(true);
                }
            } else if (id == R.id.bt_translation) {
                FabricUtil.logBtnTranslationClicked();
                if (OcrDownloadTask.checkOcrFiles(OcrNTranslateUtils.getInstance().getOcrLang())) {
                    FabricUtil.logDoBtnTranslationAction();
                    if (drawAreaView == null) {
                        Tool.logError("drawAreaView is null, ignore.");
                        return;
                    }
                    currentBoxList.addAll(drawAreaView.getAreaSelectionView().getBoxList());
                    if (SharePreferenceUtil.getInstance().isRememberLastSelection()) {
                        SharePreferenceUtil.getInstance().setLastSelectionArea(currentBoxList);
                    }
                    drawAreaView.getAreaSelectionView().clear();
                    drawAreaView.detachFromWindow();
                    drawAreaView = null;

                    if (takeScreenshot()) {
                        syncBtnState(BtnState.Translating);
                    }
                } else {
                    showDownloadOcrFileDialog(OcrNTranslateUtils.getInstance().getOcrLangDisplayName());
                }
            } else if (id == R.id.bt_clear) {
                FabricUtil.logBtnClearClicked();
                resetAll();
            }
        }
    };

    private FloatingBarMenu.OnFloatingBarMenuCallback onFloatingBarMenuCallback = new FloatingBarMenu.OnFloatingBarMenuCallback() {
        @Override
        public void onChangeModeItemClick() {
            ScreenTranslatorService.switchAppMode(AppMode.QuickWindow);
        }

        @Override
        public void onSettingItemClick() {
            new SettingView(getContext(), onSettingChangedCallback).attachToWindow();
        }

        @Override
        public void onAboutItemClick() {
            new AboutView(getContext()).attachToWindow();
        }

        @Override
        public void onHideItemClick() {
            resetAll();

            FloatingBar.this.detachFromWindow();
        }

        @Override
        public void onCloseItemClick() {
            resetAll();

            progressView = null;

            ScreenTranslatorService.stop(true);
        }

        @Override
        public void onHelpClick() {
            new HelpView(getContext()).attachToWindow();
        }
    };

    private SettingView.OnSettingChangedCallback onSettingChangedCallback = new SettingView.OnSettingChangedCallback() {
        @Override
        public void onEnableTranslationChanged(boolean enableTranslation) {
            sp_langTo.setEnabled(enableTranslation);
        }
    };

    private void resetAll() {
        if (lastAsyncTask != null && !lastAsyncTask.isCancelled()) {
            lastAsyncTask.cancel(true);
        }

        if (drawAreaView != null) {
            drawAreaView.detachFromWindow();
//            drawAreaView = null;
        }

        if (progressView != null) {
            progressView.detachFromWindow();
        }

        if (ocrResultView != null) {
            ocrResultView.detachFromWindow();
//            ocrResultView = null;
        }

        currentBoxList.clear();
        syncBtnState(BtnState.Normal);
    }

    private AreaSelectionView.OnAreaSelectionViewCallback onAreaSelectionViewCallback = new AreaSelectionView.OnAreaSelectionViewCallback() {
        @Override
        public void onAreaSelected(AreaSelectionView areaSelectionView) {
            syncBtnState(BtnState.AreaSelected);
        }
    };

    private boolean takeScreenshot() {
        final ScreenshotHandler screenshotHandler = ScreenshotHandler.getInstance();
        if (screenshotHandler.isGetUserPermission()) {
            screenshotHandler.setCallback(onScreenshotHandlerCallback);

            screenshotHandler.takeScreenshot(100);

            return true;
        } else {
            screenshotHandler.getUserPermission();
        }
        return false;
    }

    private ScreenshotHandler.OnScreenshotHandlerCallback onScreenshotHandlerCallback = new ScreenshotHandler.OnScreenshotHandlerCallback() {
        @Override
        public void onScreenshotStart() {
            FloatingBar.this.detachFromWindow(false);
        }

        @Override
        public void onScreenshotFinished(Bitmap bitmap) {
            FloatingBar.this.attachToWindow();
            showResultWindow(bitmap, currentBoxList);
        }

        @Override
        public void onScreenshotFailed(int errorCode, Throwable e) {
            FloatingBar.this.attachToWindow();
            resetAll();

            dialogView.reset();
            dialogView.setType(DialogView.Type.CONFIRM_ONLY);
            dialogView.setTitle(getContext().getString(R.string.dialog_title_error));
            String msg;
            switch (errorCode) {
                case ScreenshotHandler.ERROR_CODE_TIMEOUT:
                    msg = getContext().getString(R.string.dialog_content_screenshotTimeout);
                    break;
                case ScreenshotHandler.ERROR_CODE_IMAGE_FORMAT_ERROR:
                    msg = String.format(Locale.getDefault(), getContext().getString(R.string.dialog_content_screenshotWithImageFormatError), e.getMessage());
                    break;
                default:
                    msg = String.format(Locale.getDefault(), getContext().getString(R.string.dialog_content_screenshotWithUnknownError), e.getMessage());
                    break;
            }
            dialogView.setContentMsg(msg);
            dialogView.attachToWindow();
        }
    };

    private void showResultWindow(Bitmap screenshot, List<Rect> boxList) {
        ocrResultView = new OcrResultView(getContext(), onOcrResultViewCallback);
        ocrResultView.setOnBackButtonPressedListener(subViewOnBackButtonPressedListener);
        ocrResultView.setupHomeButtonWatcher(subViewOnHomePressedListener);
        ocrResultView.attachToWindow();
        ocrResultView.setupData(screenshot, boxList);
        FloatingBar.this.detachFromWindow(false);
        FloatingBar.this.attachToWindow();
    }

    private OcrResultView.OnOcrResultViewCallback onOcrResultViewCallback = new OcrResultView.OnOcrResultViewCallback() {
        @Override
        public void onOpenBrowserClicked() {
            syncBtnState(BtnState.Normal);
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
