package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by louis1chen on 23/10/2016.
 */

public abstract class FloatingView {
    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams floatingLayoutParams;
    private boolean isAttached = false;
    private View rootView;

    public FloatingView(Context context) {
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(getLayoutId(), null);
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        floatingLayoutParams = new WindowManager.LayoutParams(
                layoutSize(),
                layoutSize(),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        if (fullScreenMode()) {
            floatingLayoutParams.flags = floatingLayoutParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }
        floatingLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
    }

    protected abstract int getLayoutId();

    protected int layoutSize() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    protected boolean fullScreenMode() {
        return false;
    }

    public Context getContext() {
        return context;
    }

    public View getRootView() {
        return rootView;
    }

    public WindowManager.LayoutParams getFloatingLayoutParams() {
        return floatingLayoutParams;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public boolean isAttached() {
        return isAttached;
    }

    public void attachToWindow() {
        if (!isAttached) {
            windowManager.addView(rootView, floatingLayoutParams);
            isAttached = true;
        }
    }

    public void detachFromWindow() {
        if (isAttached) {
            windowManager.removeView(rootView);
            isAttached = false;
        }
    }
}
