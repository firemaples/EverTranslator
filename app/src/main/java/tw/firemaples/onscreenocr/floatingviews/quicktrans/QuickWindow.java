package tw.firemaples.onscreenocr.floatingviews.quicktrans;

import android.content.Context;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;

/**
 * Created by louis1chen on 31/01/2017.
 */

public class QuickWindow extends FloatingView {
    public QuickWindow(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_quick_window;
    }
}
