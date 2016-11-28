package tw.firemaples.onscreenocr.floatingviews;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.views.AreaSelectionView;

/**
 * Created by louis1chen on 21/10/2016.
 */

public class DrawAreaView extends FloatingView {
    private AreaSelectionView view_areaSelectionView;

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
                (AreaSelectionView) rootView.findViewById(R.id.view_areaSelectionView);
        view_areaSelectionView.setMaxRectCount(1);
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    public AreaSelectionView getAreaSelectionView() {
        return view_areaSelectionView;
    }
}
