package tw.firemaples.onscreenocr.floatings;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.MainActivity;
import tw.firemaples.onscreenocr.ScreenTranslatorService;
import tw.firemaples.onscreenocr.floatings.screencrop.RealButtonHandler;
import tw.firemaples.onscreenocr.utils.HomeWatcher;
import tw.firemaples.onscreenocr.utils.PermissionUtil;
import tw.firemaples.onscreenocr.utils.SettingUtil;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by firemaples on 23/10/2016.
 */

public abstract class FloatingView {
    private Logger logger = LoggerFactory.getLogger(FloatingView.class);

    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams floatingLayoutParams;
    private boolean isAttached = false;
    private CustomLayout rootView;
    private Object tag;
    private HomeWatcher homeWatcher;
    private OnBackButtonPressedListener onBackButtonPressedListener;
    private List<AsyncTask> manageTask = new ArrayList<>();
    private final static List<FloatingView> nonPrimaryViews = new ArrayList<>();

    public FloatingView(Context context) {
        this.context = context;
        rootView = new CustomLayout(context);
        View innerView = LayoutInflater.from(context).inflate(getLayoutId(), null);
        rootView.addView(innerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        int type;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        if (!layoutFocusable()) {
            flags = flags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        if (canMoveOutside()) {
            flags = flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        }
        floatingLayoutParams = new WindowManager.LayoutParams(
                getLayoutSize(), getLayoutSize(), type, flags, PixelFormat.TRANSLUCENT);
        if (fullScreenMode()) {
            floatingLayoutParams.flags = floatingLayoutParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }
        floatingLayoutParams.gravity = getLayoutGravity();

        if (isPrimaryView()) {
            Integer[] lastPosition = SettingUtil.INSTANCE.getLastMainBarPosition();
            if (lastPosition[0] != null && lastPosition[0] != -1) {
                floatingLayoutParams.x = lastPosition[0];
            }
            if (lastPosition[1] != null && lastPosition[1] != -1) {
                floatingLayoutParams.y = lastPosition[1];
            }
        }
    }

    protected boolean canMoveOutside() {
        return false;
    }

    protected boolean isPrimaryView() {
        return false;
    }

    protected boolean layoutFocusable() {
        return false;
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

    public void onViewStart() {
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
            if (PermissionUtil.canDrawOverlays(context)) {
                windowManager.addView(rootView, floatingLayoutParams);
                isAttached = true;
                if (!isPrimaryView()) {
                    synchronized (nonPrimaryViews) {
                        nonPrimaryViews.add(this);
                    }
                }
            } else {
                MainActivity.start(context);
                ScreenTranslatorService.stop(true);
            }
        }
    }

    public void detachFromWindow() {
        removeHomeButtonWatcher();

        if (isAttached) {
            for (AsyncTask asyncTask : manageTask) {
                if (asyncTask != null && !asyncTask.isCancelled()) {
                    asyncTask.cancel(true);
                }
            }
            manageTask.clear();

            if (isPrimaryView()) {
                SettingUtil.INSTANCE.setLastMainBarPosition(
                        new Integer[]{floatingLayoutParams.x, floatingLayoutParams.y});
            }

            windowManager.removeView(rootView);
            isAttached = false;
            if (!isPrimaryView()) {
                synchronized (nonPrimaryViews) {
                    nonPrimaryViews.remove(this);
                }
            }
        }
    }

    public static void detachAllNonPrimaryViews() {
        List<FloatingView> viewCopies;
        synchronized (nonPrimaryViews) {
            viewCopies = new ArrayList<>(nonPrimaryViews);
        }
        for (FloatingView viewCopy : viewCopies) {
            viewCopy.detachFromWindow();
        }
    }

    public void setRealButtonHandler(final RealButtonHandler handler) {
        setOnBackButtonPressedListener(new OnBackButtonPressedListener() {
            @Override
            public boolean onBackButtonPressed(FloatingView floatingView) {
                return handler.onBackButtonPressed();
            }
        });

        setupHomeButtonWatcher(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                handler.onHomeButtonPressed();
            }

            @Override
            public void onHomeLongPressed() {

            }
        });
    }

    public void setOnBackButtonPressedListener(OnBackButtonPressedListener onBackButtonPressedListener) {
        this.onBackButtonPressedListener = onBackButtonPressedListener;
    }

    public boolean onBackButtonPressed() {
        if (onBackButtonPressedListener != null) {
            return onBackButtonPressedListener.onBackButtonPressed(this);
        }
        return false;
    }

    public void setupHomeButtonWatcher(HomeWatcher.OnHomePressedListener onHomePressedListener) {
        if (homeWatcher == null) {
            homeWatcher = new HomeWatcher(getContext());
        }
        homeWatcher.setOnHomePressedListener(onHomePressedListener);
        homeWatcher.startWatch();
    }

    public void removeHomeButtonWatcher() {
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

    void updateViewLayout() {
        try {
            getWindowManager().updateViewLayout(getRootView(), getFloatingLayoutParams());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
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
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            FloatingView.this.onViewStart();
        }

        @Override

        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && getKeyDispatcherState() != null) {
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

    public interface OnBackButtonPressedListener {
        boolean onBackButtonPressed(FloatingView floatingView);
    }
}
