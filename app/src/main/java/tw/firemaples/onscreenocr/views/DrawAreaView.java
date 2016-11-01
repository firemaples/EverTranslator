package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.captureview.fullscreen.FullScreenCaptureAreaSelectionView;

/**
 * Created by louis1chen on 21/10/2016.
 */

public class DrawAreaView extends FloatingView {
    private FullScreenCaptureAreaSelectionView view_areaSelectionView;

    public DrawAreaView(Context context) {
        super(context);
        setViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_draw_area;
    }

    @Override
    protected boolean fullScreenMode() {
        return true;
    }

    protected void setViews(View rootView) {
        view_areaSelectionView =
                (FullScreenCaptureAreaSelectionView) rootView.findViewById(R.id.view_areaSelectionView);
        view_areaSelectionView.setMaxRectCount(1);
    }

    @Override
    protected int layoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    public FullScreenCaptureAreaSelectionView getAreaSelectionView() {
        return view_areaSelectionView;
    }
}
