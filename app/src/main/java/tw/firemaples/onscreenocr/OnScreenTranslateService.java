package tw.firemaples.onscreenocr;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;
import tw.firemaples.onscreenocr.captureview.CaptureView;
import tw.firemaples.onscreenocr.captureview.fullscreen.FullScreenCaptureView;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.utils.Tool;

public class OnScreenTranslateService extends Service implements FloatingViewListener {
    @SuppressLint("StaticFieldLeak")
    private static OnScreenTranslateService _instance;

    private FloatingViewManager mFloatingViewManager;
    private View floatingView;
    private FloatingViewManager.Options floatingViewManagerOptions;

    private CaptureView captureView;
    private ScreenshotHandler screenshotHandler;

    private View.OnClickListener onFloatingViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onFloatingViewClick();
        }
    };

    public OnScreenTranslateService() {
    }

    public static boolean isRunning(Context context) {
        return Tool.isServiceRunning(context, OnScreenTranslateService.class);
    }

    public static void start(Context context) {
        if (!isRunning(context)) {
            context.startService(new Intent(context, OnScreenTranslateService.class));
        }
    }

    public static void stop() {
        if (_instance != null) {
            _instance.stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _instance = this;
        int startCommand = super.onStartCommand(intent, flags, startId);
        Tool.init(this);

        floatingView = View.inflate(this, R.layout.widget_floating_button, null);
        floatingView.setOnClickListener(onFloatingViewClickListener);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.close_circle_outline);
//        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);

        floatingViewManagerOptions = new FloatingViewManager.Options();
        floatingViewManagerOptions.shape = FloatingViewManager.SHAPE_CIRCLE;
        floatingViewManagerOptions.overMargin = (int) (16 * metrics.density);
//        floatingViewManagerOptions.floatingViewX = metrics.widthPixels;
//        floatingViewManagerOptions.floatingViewY = metrics.heightPixels - 100;
        mFloatingViewManager.addViewToWindow(floatingView, floatingViewManagerOptions);

        showFloatingView();

        captureView = FullScreenCaptureView.getNewInstance(OnScreenTranslateService.this, this);

        screenshotHandler = ScreenshotHandler.getInstance(this);

        return startCommand;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFloatingViewManager.removeAllViewToWindow();
        if (screenshotHandler != null) {
            screenshotHandler.release();
            screenshotHandler = null;
        }
        _instance = null;
    }

    private void onFloatingViewClick() {
        captureView.showView();
        hideFloatingView();
    }

    public void showFloatingView() {
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
    }

    public void hideFloatingView() {
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_HIDE_ALWAYS);
    }

    @Override
    public void onFinishFloatingView() {
        stop();
    }
}
