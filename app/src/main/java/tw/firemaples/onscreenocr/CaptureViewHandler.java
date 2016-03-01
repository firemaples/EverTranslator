package tw.firemaples.onscreenocr;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;

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
    private View bt_captureViewPageClose, bt_captureViewPageClearAll, bt_captureViewPageTranslate, view_progress;
    private TextView tv_progressMsg;

    private boolean isShown = false;
    private boolean isProgressing = false;
    private OnCaptureViewHandlerCallback callback;

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
        view_progress = rootView.findViewById(R.id.view_progress);
        tv_progressMsg = (TextView) rootView.findViewById(R.id.tv_progressMsg);

        bt_captureViewPageClose.setOnClickListener(onClickListener);
        bt_captureViewPageClearAll.setOnClickListener(onClickListener);
        bt_captureViewPageTranslate.setOnClickListener(onClickListener);
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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_captureViewPageClose) {
                if (isProgressing) {
                    setProgressMode(false, null);

                    return;
                }
                hideView();
                if (callback != null)
                    callback.onCaptureViewHandlerCloseClick();
            } else if (id == R.id.bt_captureViewPageClearAll) {
                captureAreaSelectionView.clear();
            } else if (id == R.id.bt_captureViewPageTranslate) {
//                captureAreaSelectionView.setVisibility(View.GONE);
//                captureAreaSelectionView.disable();

                List<Rect> boxList = captureAreaSelectionView.getBoxList();

//                bt_captureViewPageTranslate.setEnabled(false);
//                bt_captureViewPageClearAll.setEnabled(false);

                setProgressMode(true, null);
            }
        }
    };
}

interface OnCaptureViewHandlerCallback {
    void onCaptureViewHandlerCloseClick();
}
