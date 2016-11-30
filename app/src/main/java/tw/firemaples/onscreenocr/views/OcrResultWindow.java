package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
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
    private final Context context;
    private ViewGroup parent;
    private View rootView;
    private TextView tv_originText, tv_translatedText;
    private FrameLayout.LayoutParams layoutParams;

    public OcrResultWindow(Context context, ViewGroup parent) {
        this.context = context;
        this.parent = parent;
        rootView = View.inflate(context, R.layout.view_ocr_result_window, null);

        tv_originText = (TextView) rootView.findViewById(R.id.tv_originText);
        tv_translatedText = (TextView) rootView.findViewById(R.id.tv_translatedText);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setOcrResult(OcrResult ocrResult) {
        tv_originText.setText(Tool.replaceAllLineBreaks(ocrResult.getText(), " "));
        tv_translatedText.setText(ocrResult.getTranslatedText());
    }

    public void show(View anchorView) {
        dismiss();

        new ViewPreparedWaiter().waitView(rootView, onViewPrepared);
        parent.addView(rootView, layoutParams);
    }

    public void dismiss() {
        if (rootView.getParent() != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    private ViewPreparedWaiter.OnViewPrepared onViewPrepared = new ViewPreparedWaiter.OnViewPrepared() {
        @Override
        public void onViewPrepared(View viewToWait) {
            Tool.logInfo("Width:" + viewToWait.getWidth() + " Height:" + viewToWait.getHeight());
        }
    };
}
