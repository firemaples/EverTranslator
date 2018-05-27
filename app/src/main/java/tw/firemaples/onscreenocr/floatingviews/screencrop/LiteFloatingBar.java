package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;

import java.util.List;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.ocr.OCRManager;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.translate.GoogleTranslateUtil;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;

public class LiteFloatingBar extends FloatingBar {
    private View pg_progress;

    private OCRManager ocrManager;

    public LiteFloatingBar(Context context) {
        super(context);

        ocrManager = OCRManager.getInstance(context);
        ocrManager.setListener(onOCRStateChangedListener);
    }

    @Override
    protected void setViews(View view) {
        pg_progress = view.findViewById(R.id.pg_progress);
        super.setViews(view);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_floating_bar_lite;
    }


    @Override
    public void attachToWindow() {
        super.attachToWindow();

        if (!SharePreferenceUtil.getInstance().isLiteHowToUseAlreadyShown()) {
            new HelpLiteView(getContext()).attachToWindow();
        }
    }

    @Override
    protected void onTranslateBtnClicked() {
        if (GoogleTranslateUtil.isGoogleTranslateInstalled(getContext())) {
            super.onTranslateBtnClicked();
        } else {
            resetAll();
            GoogleTranslateUtil.showGoogleTranslateNotInstallDialog(getContext());
        }
    }

    @Override
    protected void showResultWindow(Bitmap screenshot, List<Rect> boxList) {
        ocrManager.start(screenshot, boxList);
    }

    private OCRManager.OnOCRStateChangedListener onOCRStateChangedListener =
            new OCRManager.OnOCRStateChangedListener() {
                @Override
                public void onInitializing() {

                }

                @Override
                public void onInitialized() {

                }

                @Override
                public void onRecognizing() {

                }

                @Override
                public void onRecognized(List<OcrResult> results) {
                    String lang = OcrNTranslateUtils.getInstance().getOcrLang();
                    if (results.size() > 0) {
                        GoogleTranslateUtil.start(getContext(), lang, results.get(0).getText());
                    }
                    resetAll();
                }
            };

    @Override
    protected void syncBtnState(BtnState btnState) {
        switch (btnState) {
            case Normal:
                bt_selectArea.setVisibility(View.VISIBLE);
                bt_selectArea.setEnabled(true);
                bt_translation.setVisibility(View.GONE);
                pg_progress.setVisibility(View.GONE);
                break;
            case AreaSelecting:
                bt_selectArea.setVisibility(View.GONE);
                bt_translation.setVisibility(View.VISIBLE);
                bt_translation.setEnabled(false);
                pg_progress.setVisibility(View.GONE);
                break;
            case AreaSelected:
                bt_selectArea.setVisibility(View.GONE);
                bt_translation.setVisibility(View.VISIBLE);
                bt_translation.setEnabled(true);
                pg_progress.setVisibility(View.GONE);
                break;
            case Translating:
                bt_selectArea.setVisibility(View.GONE);
                bt_selectArea.setEnabled(false);
                bt_translation.setVisibility(View.GONE);
                pg_progress.setVisibility(View.VISIBLE);
                break;
            case Translated:
                bt_selectArea.setVisibility(View.VISIBLE);
                bt_selectArea.setEnabled(true);
                bt_translation.setVisibility(View.GONE);
                pg_progress.setVisibility(View.GONE);
                break;
        }
    }
}
