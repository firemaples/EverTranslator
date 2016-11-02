package tw.firemaples.onscreenocr;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.views.FloatingBar;

/**
 * Created by louis1chen on 21/10/2016.
 */

public class ScreenTranslatorService extends Service {

    @SuppressLint("StaticFieldLeak")
    private static ScreenTranslatorService _instance;

    private ScreenshotHandler screenshotHandler;

    private WindowManager mWindowManager;
    private FloatingBar floatingBar;
    private WindowManager.LayoutParams floatingBarLayoutParams;
    private boolean mIsFloatingViewAttached = false;

    public ScreenTranslatorService() {
    }

    public static boolean isRunning(Context context) {
        return Tool.isServiceRunning(context, ScreenTranslatorService.class);
    }

    public static void start(Context context) {
        if (!isRunning(context)) {
            context.startService(new Intent(context, ScreenTranslatorService.class));
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

        return startCommand;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Tool.init(this);
        screenshotHandler = ScreenshotHandler.getInstance();
        OcrNTranslateUtils.init(this);

        floatingBar = new FloatingBar(this);
        floatingBar.attachToWindow();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        floatingBar.detachFromWindow();

        if (screenshotHandler != null) {
            screenshotHandler.release();
            screenshotHandler = null;
        }
        _instance = null;
    }
}
