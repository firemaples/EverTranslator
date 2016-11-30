package tw.firemaples.onscreenocr;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.view.WindowManager;

import tw.firemaples.onscreenocr.floatingviews.FloatingBar;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 21/10/2016.
 */

public class ScreenTranslatorService extends Service {
    private final int ONGOING_NOTIFICATION_ID = 12345;

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

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground();
        Tool.init(this);
        screenshotHandler = ScreenshotHandler.getInstance();
        OcrNTranslateUtils.init(this);

        floatingBar = new FloatingBar(this);
        floatingBar.attachToWindow();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground();

        floatingBar.detachFromWindow();

        if (screenshotHandler != null) {
            screenshotHandler.release();
            screenshotHandler = null;
        }
        _instance = null;
    }

    private void startForeground() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon));
        builder.setTicker(getString(R.string.app_name));
        builder.setContentTitle(getString(R.string.app_name));
//        builder.setContentText(getString(R.string.app_name));
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    private void stopForeground() {
        stopForeground(true);
    }
}
