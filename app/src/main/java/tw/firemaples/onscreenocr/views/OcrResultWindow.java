package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.utils.ViewPreparedWaiter;

/**
 * Created by louis1chen on 29/11/2016.
 */

public class OcrResultWindow {
    private final static int MARGIN = 10;

    private final Context context;
    private ViewGroup parent;
    private View anchorView;

    private View rootView;
    private TextView tv_originText, tv_translatedText;
    private FrameLayout.LayoutParams layoutParams;
    private DisplayMetrics metrics;

    public OcrResultWindow(Context context, ViewGroup parent) {
        this.context = context;
        this.parent = parent;
        rootView = View.inflate(context, R.layout.view_ocr_result_window, null);

        tv_originText = (TextView) rootView.findViewById(R.id.tv_originText);
        tv_translatedText = (TextView) rootView.findViewById(R.id.tv_translatedText);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);
    }

    public void setOcrResult(OcrResult ocrResult) {
        tv_originText.setText(Tool.replaceAllLineBreaks(ocrResult.getText(), " "));
        tv_translatedText.setText(ocrResult.getTranslatedText());
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
}
