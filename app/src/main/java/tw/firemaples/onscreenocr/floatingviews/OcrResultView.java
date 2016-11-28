package tw.firemaples.onscreenocr.floatingviews;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.views.OrcResultsDrawerView;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 01/11/2016.
 */

public class OcrResultView extends FloatingView {
    private OrcResultsDrawerView view_ocrResultView;

    public OcrResultView(Context context) {
        super(context);
        setViews(getRootView());
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
        view_ocrResultView = (OrcResultsDrawerView) rootView.findViewById(R.id.view_ocrResultView);
    }

    public void setOcrResults(List<OcrResult> results) {
        view_ocrResultView.setOcrResults(results);

        if (Tool.getInstance().isDebugMode() && results.size() > 0) {
            setDebugInfo(results.get(0));
        }
    }

    private void setDebugInfo(OcrResult ocrResult) {
        TextView tv_debugInfo = (TextView) getRootView().findViewById(R.id.tv_debugInfo);
        String[] infoArray = ocrResult.getDebugInfo().getInfoList().toArray(new String[ocrResult.getDebugInfo().getInfoList().size()]);
        String debugInfoString = TextUtils.join("\n", infoArray);
        tv_debugInfo.setText(debugInfoString);
        tv_debugInfo.setVisibility(View.VISIBLE);
    }

    public void clear() {
        view_ocrResultView.clear();
    }
}
