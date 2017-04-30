package tw.firemaples.onscreenocr.floatingviews;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.MainActivity;
import tw.firemaples.onscreenocr.ScreenTranslatorService;
import tw.firemaples.onscreenocr.utils.HomeWatcher;
import tw.firemaples.onscreenocr.utils.PermissionUtil;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by firemaples on 23/10/2016.
 */

public abstract class FloatingView {
    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams floatingLayoutParams;
    private boolean isAttached = false;
    private CustomLayout rootView;
    private Object tag;
    private HomeWatcher homeWatcher;
    private List<AsyncTask> manageTask = new ArrayList<>();

    public FloatingView(Context context) {
        this.context = context;
        rootView = new CustomLayout(context);
        View innerView = LayoutInflater.from(context).inflate(getLayoutId(), null);
        rootView.addView(innerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        floatingLayoutParams = new WindowManager.LayoutParams(
                getLayoutSize(),
                getLayoutSize(),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        if (fullScreenMode()) {
            floatingLayoutParams.flags = floatingLayoutParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }
        floatingLayoutParams.gravity = getLayoutGravity();
    }

    protected abstract int getLayoutId();

    protected int getLayoutSize() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    protected int getLayoutGravity() {
        return Gravity.TOP | Gravity.LEFT;
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

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public void manageTask(AsyncTask asyncTask) {
        manageTask.add(asyncTask);
    }

    public void attachToWindow() {
        if (!isAttached) {
            if (PermissionUtil.checkDrawOverlayPermission(context)) {
                windowManager.addView(rootView, floatingLayoutParams);
                isAttached = true;
            } else {
                MainActivity.start(context);
                ScreenTranslatorService.stop(true);
            }
        }
    }

    public void detachFromWindow() {
        if (isAttached) {
            for (AsyncTask asyncTask : manageTask) {
                if (asyncTask != null && !asyncTask.isCancelled()) {
                    asyncTask.cancel(true);
                }
            }
            manageTask.clear();

            windowManager.removeView(rootView);
            isAttached = false;

            removeHomeButtonWatcher();
        }
    }

    public boolean onBackButtonPressed() {
        return false;
    }

    protected void setupHomeButtonWatcher(HomeWatcher.OnHomePressedListener onHomePressedListener) {
        if (homeWatcher == null) {
            homeWatcher = new HomeWatcher(getContext());
        }
        homeWatcher.setOnHomePressedListener(onHomePressedListener);
        homeWatcher.startWatch();
    }

    protected void removeHomeButtonWatcher() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (homeWatcher != null) {
                        homeWatcher.setOnHomePressedListener(null);
                        homeWatcher.stopWatch();
                        homeWatcher = null;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    private class CustomLayout extends LinearLayout {

        public CustomLayout(Context context) {
            super(context);
        }

        public CustomLayout(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public CustomLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public CustomLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    getKeyDispatcherState().startTracking(event, this);
                    return true;

                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    getKeyDispatcherState().handleUpEvent(event);

                    if (event.isTracking() && !event.isCanceled()) {
                        // dismiss your window:
                        if (onBackButtonPressed()) {
                            return true;
                        }
                    }
                }
            }

            return super.dispatchKeyEvent(event);
        }
    }
}
