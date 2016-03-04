package tw.firemaples.onscreenocr;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import tw.firemaples.onscreenocr.utils.Tool;

/**
 * android.permission.SYSTEM_ALERT_WINDOW
 *
 * @author Firemaples
 */
public class FloatingNotification {
    Context context;
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;

    @SuppressWarnings("unused")
    private boolean mHasDoubleClicked = false;
    private long lastPressTime;
    private OnFloatingNotificationTapListener onFloatingNotificationTapListener;
    private boolean isShowing = false;
    private boolean isMoving = false;

    public FloatingNotification(Context context, View floatingView) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        params.x = metrics.widthPixels;

        createFloatingView(context, floatingView, params);
    }

    public FloatingNotification(Context context, View floatingView,
                                WindowManager.LayoutParams params) {
        createFloatingView(context, floatingView, params);
    }

    private void createFloatingView(Context context, View floatingView,
                                    WindowManager.LayoutParams params) {
        this.context = context;
        this.floatingView = floatingView;
        this.params = params;
        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        iniTouchEvent();
    }

    private void iniTouchEvent() {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private WindowManager.LayoutParams paramsF = params;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Tool.LogInfo("Action Down");
                        // Get current time in nano seconds.
                        long pressTime = System.currentTimeMillis();

                        // If double click...
                        if (pressTime - lastPressTime <= 300) {
                            if (onFloatingNotificationTapListener != null)
                                onFloatingNotificationTapListener
                                        .onDoubleClick(floatingView);
                            mHasDoubleClicked = true;
                        } else { // If not double click....
                            mHasDoubleClicked = false;
                        }
                        lastPressTime = pressTime;
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        Tool.LogInfo("Action Up");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isMoving = false;
                            }
                        }, 300);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Tool.LogInfo("Action Move");
                        paramsF.x = initialX
                                + (int) (event.getRawX() - initialTouchX);
                        paramsF.y = initialY
                                + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, paramsF);

                        Tool.LogInfo("x:" + (initialX - paramsF.x) + ", y:" + (initialY - paramsF.y));
                        if (!isMoving && Math.abs(initialX - paramsF.x) > 5 && Math.abs(initialY - paramsF.y) > 5) {
                            isMoving = true;
                        }
                        break;
                }
                return false;
            }
        });

        floatingView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!isMoving && onFloatingNotificationTapListener != null)
                    onFloatingNotificationTapListener.onClick(floatingView);
            }
        });

        floatingView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (!isMoving && onFloatingNotificationTapListener != null)
                    onFloatingNotificationTapListener.onLongClick(floatingView);
                return false;
            }
        });
    }

    public void setOnFloatingNotificationTapListener(
            OnFloatingNotificationTapListener onFloatingNotificationTapListener) {
        this.onFloatingNotificationTapListener = onFloatingNotificationTapListener;
    }

    public FloatingNotification show() {
        if (!isShowing) {
            windowManager.addView(floatingView, params);
            isShowing = true;
        }

        return this;
    }

    public FloatingNotification hide() {
        if (floatingView != null && floatingView.isShown())
            windowManager.removeView(floatingView);
        isShowing = false;

        return this;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public FloatingNotification toggle() {
        if (isShowing())
            hide();
        else
            show();

        return this;
    }

    public View getFloatingView() {
        return floatingView;
    }

    public WindowManager.LayoutParams getLayoutParams() {
        return params;
    }

    public void setLayoutParams(WindowManager.LayoutParams params) {
        this.params = params;
    }

    public void updateFloatingView() {
        windowManager.updateViewLayout(floatingView, params);
    }

    public interface OnFloatingNotificationTapListener {
        void onClick(View floatingView);

        void onLongClick(View floatingView);

        void onDoubleClick(View floatingView);
    }
}
