package tw.firemaples.onscreenocr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.Arrays;
import java.util.List;

import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by Firemaples on 2016/3/1.
 */
public class CaptureViewHandler {
    private static CaptureViewHandler captureViewHandler;

    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    private View rootView;
    private CaptureAreaSelectionView captureAreaSelectionView;
    private View bt_captureViewPageClose, bt_captureViewPageClearAll, bt_captureViewPageTranslate, bt_captureViewPageSettings,
            view_progress;
    private TextView tv_progressMsg;

    private boolean isShown = false;
    private boolean isProgressing = false;
    private OnCaptureViewHandlerCallback callback;

    private TessBaseAPI baseAPI;
    private ProgressDialog progressDialog;

    public static CaptureViewHandler getInstance(Context context) {
        if (captureViewHandler == null) captureViewHandler = new CaptureViewHandler(context);
        return captureViewHandler;
    }

    private CaptureViewHandler(Context context) {
        this.context = context;
        this.rootView = LayoutInflater.from(context).inflate(R.layout.view_capture_page, null, false);
        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;

        iniViews();
    }

    private void iniViews() {
        bt_captureViewPageClose = rootView.findViewById(R.id.bt_captureViewPageClose);
        bt_captureViewPageClearAll = rootView.findViewById(R.id.bt_captureViewPageClearAll);
        bt_captureViewPageTranslate = rootView.findViewById(R.id.bt_captureViewPageTranslate);
        bt_captureViewPageSettings = rootView.findViewById(R.id.bt_captureViewPageSettings);
        view_progress = rootView.findViewById(R.id.view_progress);
        tv_progressMsg = (TextView) rootView.findViewById(R.id.tv_progressMsg);

        bt_captureViewPageClose.setOnClickListener(onClickListener);
        bt_captureViewPageClearAll.setOnClickListener(onClickListener);
        bt_captureViewPageTranslate.setOnClickListener(onClickListener);
        bt_captureViewPageSettings.setOnClickListener(onClickListener);
        captureAreaSelectionView = (CaptureAreaSelectionView) rootView.findViewById(R.id.captureAreaSelectionView);
    }

    public void setCallback(OnCaptureViewHandlerCallback callback) {
        this.callback = callback;
    }

    public void showView() {
        if (!isShown) {
            isShown = true;
            windowManager.addView(rootView, params);
        }
    }

    public void hideView() {
        if (isShown) {
            isShown = false;
            windowManager.removeView(rootView);
        }
    }

    public void setProgressMode(boolean progress, String message) {
        this.isProgressing = progress;
        if (progress) {
            view_progress.setVisibility(View.VISIBLE);
            tv_progressMsg.setText(message == null ? context.getString(R.string.progressProcessingDefaultMessage) : message);
        } else {
            view_progress.setVisibility(View.GONE);
        }
    }

    public void initOrcEngine() {
        setProgressMode(true, "Waiting for initialize...");
        if (baseAPI == null) {
            baseAPI = new TessBaseAPI();
        }

        String recognitionLang = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.KEY_RECOGNITION_LANGUAGE, "eng");

        int langIndex = Arrays.asList(context.getResources().getStringArray(R.array.iso6393)).indexOf(recognitionLang);
        String langName = context.getResources().getStringArray(R.array.languagenames)[langIndex];

        new OrcInitAsyncTask(context, baseAPI, recognitionLang, langName, this).setCallback(new OrcInitAsyncTask.OnOrcInitAsyncTaskCallback() {
            @Override
            public void onOrcInitialized() {
                startOrc();
            }
        }).execute();
    }

    public void startOrc() {
        List<Rect> boxList = captureAreaSelectionView.getBoxList();
        new TextRecognizeAsyncTask(context, baseAPI, this, boxList).setCallback(new TextRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback() {
            boolean tempIsShown = false;

            @Override
            public void onTextRecognizeFinished() {
                Tool.ShowMsg(context, "TextRecognizeFinished!");
            }

            @Override
            public void onCaptureScreenStart() {
                if (callback != null)
                    callback.onCaptureScreenStart();
                tempIsShown = isShown;
                if (tempIsShown)
                    hideView();
            }

            @Override
            public void onCaptureScreenEnd() {
                if (callback != null)
                    callback.onCaptureScreenEnd();
                if (tempIsShown)
                    showView();
            }
        }).execute();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_captureViewPageClose) {
//                if (isProgressing) {
//                    setProgressMode(false, null);
//
//                    return;
//                }
                hideView();
                if (callback != null)
                    callback.onCaptureViewHandlerCloseClick();
            } else if (id == R.id.bt_captureViewPageClearAll) {
                captureAreaSelectionView.clear();
            } else if (id == R.id.bt_captureViewPageTranslate) {
//                captureAreaSelectionView.setVisibility(View.GONE);
//                captureAreaSelectionView.disable();

                if (captureAreaSelectionView.getBoxList().size() == 0) {
                    Tool.LogError("Please draw area before recognize");
                    Tool.ShowErrorMsg(context, "Please draw area before recognize");
                } else {
                    initOrcEngine();
                }

//                bt_captureViewPageTranslate.setEnabled(false);
//                bt_captureViewPageClearAll.setEnabled(false);
            } else if (id == R.id.bt_captureViewPageSettings) {
                context.startActivity(SettingsActivity.getIntent(context)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                hideView();
            }
        }
    };

    public interface OnCaptureViewHandlerCallback {
        void onCaptureViewHandlerCloseClick();

        void onCaptureScreenStart();

        void onCaptureScreenEnd();
    }
}
