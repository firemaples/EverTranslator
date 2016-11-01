package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.captureview.fullscreen.FullScreenOrcResultsView;
import tw.firemaples.onscreenocr.ocr.OcrResult;

/**
 * Created by louis1chen on 01/11/2016.
 */

public class OcrResultView extends FloatingView {
    private FullScreenOrcResultsView view_ocrResultView;

    public OcrResultView(Context context) {
        super(context);
        setViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_result_view;
    }

    @Override
    protected int layoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected boolean fullScreenMode() {
        return true;
    }

    private void setViews(View rootView) {
        view_ocrResultView = (FullScreenOrcResultsView) rootView.findViewById(R.id.view_ocrResultView);
    }

    public void setOcrResults(List<OcrResult> results) {
        view_ocrResultView.setOcrResults(results);
    }

    public void clear() {
        view_ocrResultView.clear();
    }
}
