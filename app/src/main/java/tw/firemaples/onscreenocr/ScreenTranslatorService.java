package tw.firemaples.onscreenocr;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.floatingviews.quicktrans.QuickWindow;
import tw.firemaples.onscreenocr.floatingviews.screencrop.FloatingBar;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.utils.AppMode;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 21/10/2016.
 */

public class ScreenTranslatorService extends Service {
    private static final int ONGOING_NOTIFICATION_ID = 12345;

    @SuppressLint("StaticFieldLeak")
    private static ScreenTranslatorService _instance;

    private ScreenshotHandler screenshotHandler;
    private SharePreferenceUtil spUtil;

    private FloatingView mainFloatingView;
    private boolean dismissNotify = true;

    public ScreenTranslatorService() {
        _instance = this;
    }

    public static Context getContext() {
        if (_instance != null) {
            return _instance;
        }
        return null;
    }

    public static boolean isRunning(Context context) {
        return Tool.isServiceRunning(context, ScreenTranslatorService.class);
    }

    public static void start(Context context, boolean fromNotify) {
        if (!isRunning(context)) {
            context.startService(new Intent(context, ScreenTranslatorService.class));
        } else if (fromNotify && _instance != null) {
            _instance._startFloatingView();
        }
    }

    public static void stop(boolean dismissNotify) {
        if (_instance != null) {
            _instance.dismissNotify = dismissNotify;
            _instance.stopSelf();
        }
    }

    public static void switchAppMode(AppMode appMode) {
        if (_instance != null && SharePreferenceUtil.getInstance().getAppMode() != appMode) {
            _instance._stopFloatingView();
            SharePreferenceUtil.getInstance().setAppMode(appMode);
            _instance._startFloatingView();
        }
    }

    public static void resetForeground() {
        if (_instance != null) {
            _instance.stopForeground();
            _instance.startForeground();
        }
    }

    public static void startFloatingView() {
        if (_instance != null) {
            _instance._startFloatingView();
        }
    }

    public static void stopFloatingView() {
        if (_instance != null) {
            _instance._stopFloatingView();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (_instance == null) {
            _instance = this;
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (_instance == null) {
            _instance = this;
        }
        if (!Fabric.isInitialized()) {
            Fabric.with(this, new Crashlytics());
        }

        Tool.init(this);
        spUtil = SharePreferenceUtil.getInstance();
        screenshotHandler = ScreenshotHandler.getInstance();
        OcrNTranslateUtils.init();

        _startFloatingView();

        startForeground();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground();

        _stopFloatingView();

        if (screenshotHandler != null) {
            screenshotHandler.release();
            screenshotHandler = null;
        }
        _instance = null;
    }

    private void startForeground() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.notify_icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon));
        builder.setTicker(getString(R.string.app_name));
        builder.setContentTitle(getString(R.string.app_name));
        boolean toShow = mainFloatingView == null || !mainFloatingView.isAttached();
        if (!toShow) {
            builder.setContentText(getString(R.string.notification_contentText_hide));
        } else {
            builder.setContentText(getString(R.string.notification_contentText_show));
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        notificationIntent.putExtra(MainActivity.INTENT_START_FROM_NOTIFY, true);
        notificationIntent.putExtra(MainActivity.INTENT_SHOW_FLOATING_VIEW, toShow);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    private void stopForeground() {
        stopForeground(dismissNotify);
    }

    private void _startFloatingView() {
        if (mainFloatingView != null && mainFloatingView.isAttached()) {
            return;
        }
        switch (spUtil.getAppMode()) {
            case ScreenCrop:
                if (!(mainFloatingView instanceof FloatingBar)) {
                    mainFloatingView = new FloatingBar(this);
                }
                break;
            case QuickWindow:
                if (!(mainFloatingView instanceof QuickWindow)) {
                    mainFloatingView = new QuickWindow(this);
                }
                break;
        }
        mainFloatingView.attachToWindow();
    }

    private void _stopFloatingView() {
        mainFloatingView.detachFromWindow();
    }
}
