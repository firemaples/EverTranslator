package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.firemaples.onscreenocr.MainActivity;
import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.ScreenTranslatorService;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.floatingviews.MovableFloatingView;
import tw.firemaples.onscreenocr.ocr.OCRDownloadTask;
import tw.firemaples.onscreenocr.ocr.OCRLangUtil;
import tw.firemaples.onscreenocr.ocr.OnOCRDownloadTaskCallback;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.utils.HomeWatcher;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SettingUtil;
import tw.firemaples.onscreenocr.views.AreaSelectionView;
import tw.firemaples.onscreenocr.views.FloatingBarMenu;

/**
 * Created by firemaples on 21/10/2016.
 */

public abstract class FloatingBar extends MovableFloatingView {
    private static final Logger logger = LoggerFactory.getLogger(FloatingBar.class);

    protected View view_menu, bt_selectArea, bt_translation, bt_clear;
    protected Spinner sp_langFrom, sp_langTo;

    private BtnState btnState = BtnState.Normal;

    private DialogView dialogView;
    private DrawAreaView drawAreaView;
    private List<Rect> currentBoxList = new ArrayList<>();

    //OCR
    private OcrNTranslateUtils ocrNTranslateUtils;
    private OcrResultView ocrResultView;
    private OCRDownloadTask ocrDownloadTask = OCRDownloadTask.INSTANCE;

    public FloatingBar(Context context) {
        super(context);
        ocrNTranslateUtils = OcrNTranslateUtils.getInstance();
        setViews(getRootView());
    }

    @Override
    public void attachToWindow() {
        super.attachToWindow();
        SettingUtil.INSTANCE.setAppShowing(true);

        if (!SettingUtil.INSTANCE.isVersionHistoryAlreadyShown()) {
            new VersionHistoryView(getContext()).attachToWindow();
        }
    }

    private void detachFromWindow(boolean reset) {
        SettingUtil.INSTANCE.setAppShowing(false);
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
            resetAll();
            return true;
        }
        return super.onBackButtonPressed();
    }

    @Override
    public void detachFromWindow() {
        SettingUtil.INSTANCE.setAppShowing(false);
        resetAll();
        super.detachFromWindow();
        ScreenTranslatorService.resetForeground();
    }

    @Override
    protected int getLayoutGravity() {
        return Gravity.TOP | Gravity.RIGHT;
    }

    protected void setViews(View rootView) {
        view_menu = rootView.findViewById(R.id.view_menu);
        bt_selectArea = rootView.findViewById(R.id.bt_selectArea);
        bt_translation = rootView.findViewById(R.id.bt_translation);
        bt_clear = rootView.findViewById(R.id.bt_clear);
        sp_langFrom = rootView.findViewById(R.id.sp_langFrom);
        sp_langTo = rootView.findViewById(R.id.sp_langTo);

        view_menu.setOnClickListener(onClickListener);
        bt_selectArea.setOnClickListener(onClickListener);
        bt_translation.setOnClickListener(onClickListener);
        if (bt_clear != null) {
            bt_clear.setOnClickListener(onClickListener);
        }

        nextBtnState(BtnState.Normal);

        sp_langFrom.setSelection(OCRLangUtil.INSTANCE.getSelectedLangIndex());
        sp_langFrom.setOnItemSelectedListener(onItemSelectedListener);
        if (sp_langTo != null) {
            sp_langTo.setSelection(ocrNTranslateUtils.getTranslateToIndex());
            sp_langTo.setEnabled(SettingUtil.INSTANCE.getEnableTranslation());
            sp_langTo.setOnItemSelectedListener(onItemSelectedListener);
        }

        dialogView = new DialogView(getContext());

        setDragView(view_menu);

        if (SettingUtil.INSTANCE.getStartingWithSelectionMode()) {
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

                if (!OCRDownloadTask.INSTANCE.checkOCRFileExists(lang)) {
                    showDownloadOcrFileDialog(OCRLangUtil.INSTANCE.getSelectedLangName());
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
            public void onConfirmClick(DialogView dialogView) {
                super.onConfirmClick(dialogView);
                ocrDownloadTask.downloadOCRFiles(OCRLangUtil.INSTANCE.getSelectedLangCode(), onOcrDownloadAsyncTaskCallback);
            }
        });
        dialogView.attachToWindow();
    }

    private OnOCRDownloadTaskCallback onOcrDownloadAsyncTaskCallback = new OnOCRDownloadTaskCallback() {
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
            logger.info("onClick");
            int id = v.getId();
            if (id == R.id.view_menu) {
                new FloatingBarMenu(getContext(), view_menu, onFloatingBarMenuCallback).show();
            } else if (id == R.id.bt_selectArea) {
                if (ScreenshotHandler.isInitialized()) {
                    drawAreaView = new DrawAreaView(getContext());
                    drawAreaView.setOnBackButtonPressedListener(subViewOnBackButtonPressedListener);
                    drawAreaView.setupHomeButtonWatcher(subViewOnHomePressedListener);
                    drawAreaView.attachToWindow();
                    FloatingBar.this.detachFromWindow(false);
                    FloatingBar.this.attachToWindow();
                    nextBtnState(BtnState.AreaSelecting);
                    drawAreaView.getAreaSelectionView().setCallback(onAreaSelectionViewCallback);
                    if (SettingUtil.INSTANCE.isRememberLastSelection()) {
                        drawAreaView.getAreaSelectionView().setBoxList(SettingUtil.INSTANCE.getLastSelectionArea());
                    }
                } else {
                    MainActivity.start(getContext());
                    ScreenTranslatorService.stop(true);
                }
            } else if (id == R.id.bt_translation) {
                onTranslateBtnClicked();
            } else if (id == R.id.bt_clear) {
                resetAll();
            }
        }
    };

    private FloatingBarMenu.OnFloatingBarMenuCallback onFloatingBarMenuCallback = new FloatingBarMenu.OnFloatingBarMenuCallback() {

        @Override
        public void onSettingItemClick() {
            new SettingView(getContext()).attachToWindow();
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

            ScreenTranslatorService.stop(true);
        }

        @Override
        public void onHelpClick() {
            new HelpView(getContext()).attachToWindow();
        }
    };

    protected void resetAll() {
        if (drawAreaView != null) {
            drawAreaView.detachFromWindow();
//            drawAreaView = null;
        }

        if (ocrResultView != null) {
            ocrResultView.detachFromWindow();
//            ocrResultView = null;
        }

        currentBoxList.clear();
        nextBtnState(BtnState.Normal);
    }

    private AreaSelectionView.OnAreaSelectionViewCallback onAreaSelectionViewCallback = new AreaSelectionView.OnAreaSelectionViewCallback() {
        @Override
        public void onAreaSelected(AreaSelectionView areaSelectionView) {
            nextBtnState(BtnState.AreaSelected);
        }
    };

    protected void onTranslateBtnClicked() {
        if (OCRDownloadTask.INSTANCE.checkOCRFileExists(OCRLangUtil.INSTANCE.getSelectedLangCode())) {
            if (drawAreaView == null) {
                logger.error("drawAreaView is null, ignore.");
                return;
            }
            currentBoxList.addAll(drawAreaView.getAreaSelectionView().getBoxList());
            if (SettingUtil.INSTANCE.isRememberLastSelection()) {
                SettingUtil.INSTANCE.setLastSelectionArea(currentBoxList);
            }
            drawAreaView.getAreaSelectionView().clear();
            drawAreaView.detachFromWindow();
            drawAreaView = null;

            if (takeScreenshot()) {
                nextBtnState(BtnState.Translating);
            }
        } else {
            showDownloadOcrFileDialog(OCRLangUtil.INSTANCE.getSelectedLangName());
        }
    }

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

    protected void showResultWindow(Bitmap screenshot, List<Rect> boxList) {
        ocrResultView = new OcrResultView(getContext(), onOcrResultViewCallback);
        ocrResultView.setOnBackButtonPressedListener(subViewOnBackButtonPressedListener);
        ocrResultView.setupHomeButtonWatcher(subViewOnHomePressedListener);
        ocrResultView.attachToWindow();
//        ocrResultView.setupData(screenshot, boxList);
        FloatingBar.this.detachFromWindow(false);
        FloatingBar.this.attachToWindow();
    }

    private OcrResultView.OnOcrResultViewCallback onOcrResultViewCallback = new OcrResultView.OnOcrResultViewCallback() {
        @Override
        public void onOpenBrowserClicked() {
            nextBtnState(BtnState.Normal);
        }

        @Override
        public void onOpenGoogleTranslateClicked() {
            resetAll();
        }

        @Override
        public void onOCRTextChanged(String newText) {
        }
    };

    private void nextBtnState(BtnState btnState) {
        this.btnState = btnState;
        syncBtnState(btnState);
    }

    protected abstract void syncBtnState(BtnState btnState);

    protected enum BtnState {
        Normal, AreaSelecting, AreaSelected, Translating, Translated
    }
}
