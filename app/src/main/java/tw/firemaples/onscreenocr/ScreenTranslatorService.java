package tw.firemaples.onscreenocr;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.firemaples.onscreenocr.database.DatabaseManager;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.floatingviews.screencrop.LiteFloatingBar;
import tw.firemaples.onscreenocr.floatingviews.screencrop.NormalFloatingBar;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.tts.AndroidTTSManager;
import tw.firemaples.onscreenocr.utils.AppMode;
import tw.firemaples.onscreenocr.utils.FabricUtil;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 21/10/2016.
 */

public class ScreenTranslatorService extends Service {
    private static final int ONGOING_NOTIFICATION_ID = 12345;

    private static final Logger logger = LoggerFactory.getLogger(ScreenTranslatorService.class);

    @SuppressLint("StaticFieldLeak")
    private static ScreenTranslatorService _instance;

    private NotificationManager notificationManager;

    private ScreenshotHandler screenshotHandler;
    private SharePreferenceUtil spUtil;

    private FloatingView mainFloatingView;
    private boolean dismissNotify = false;

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
        return Tool.isServiceRunning(context, ScreenTranslatorService.class) && _instance != null;
    }

    public static void start(Context context, boolean fromNotify, boolean showFloatingView) {
        if (!isRunning(context)) {
            context.startService(new Intent(context, ScreenTranslatorService.class));
        } else if (fromNotify && _instance != null) {
            if (showFloatingView) {
                _instance._startFloatingView();
            } else {
                _instance._stopFloatingView(true);
            }
        }
    }

    public static void stop(boolean dismissNotify) {
        if (_instance != null) {
            _instance.dismissNotify = dismissNotify;
            _instance._stopFloatingView(false);
            _instance.stopSelf();
        }
    }

    public static void switchAppMode(AppMode appMode) {
        if (_instance != null && SharePreferenceUtil.getInstance().getAppMode() != appMode) {
            _instance._stopFloatingView(true);
            SharePreferenceUtil.getInstance().setAppMode(appMode);
            _instance._startFloatingView();
        }
    }

    public static void resetForeground() {
        if (_instance != null) {
            _instance.updateNotification();
        }
    }

    public static void startFloatingView() {
        if (_instance != null) {
            _instance._startFloatingView();
        }
    }

    public static void stopFloatingView() {
        if (_instance != null) {
            _instance._stopFloatingView(true);
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

        Tool.init(this);
        FabricUtil.logClientInfo();
        spUtil = SharePreferenceUtil.getInstance();
        screenshotHandler = ScreenshotHandler.getInstance();
        OcrNTranslateUtils.init();
        AndroidTTSManager.getInstance(this).init();
        DatabaseManager.getInstance();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (SharePreferenceUtil.getInstance().isAppShowing()) {
            _startFloatingView();
        }

        startForeground();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground();

        _stopFloatingView(false);

        //If process was been killed by system or user
        SharePreferenceUtil.getInstance().setIsAppShowing(true, this);

        if (screenshotHandler != null) {
            screenshotHandler.release();
            screenshotHandler = null;
        }
        _instance = null;
    }

    private void startForeground() {
        startForeground(ONGOING_NOTIFICATION_ID, getForegroundNotification());
    }

    private Notification getForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(getPackageName()) == null) {
                notificationManager.createNotificationChannel(
                        new NotificationChannel(getPackageName(),
                                getString(R.string.foregroundNotification),
                                NotificationManager.IMPORTANCE_LOW));
            }
        }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(getPackageName());
        }
        return builder.build();
    }

    private void stopForeground() {
        stopForeground(dismissNotify);
    }

    private void updateNotification() {
        if (!dismissNotify) {
            notificationManager.notify(ONGOING_NOTIFICATION_ID, getForegroundNotification());
        }
    }

    private void _startFloatingView() {
        if (mainFloatingView != null && mainFloatingView.isAttached()) {
            return;
        }
        switch (spUtil.getAppMode()) {
            case Normal:
                if (!(mainFloatingView instanceof NormalFloatingBar)) {
                    mainFloatingView = new NormalFloatingBar(this);
                }
                break;
            case Lite:
                if (!(mainFloatingView instanceof LiteFloatingBar)) {
                    mainFloatingView = new LiteFloatingBar(this);
                }
                break;
        }
        mainFloatingView.attachToWindow();
        updateNotification();
    }

    private void _stopFloatingView(boolean updateNotify) {
        if (mainFloatingView != null) {
            mainFloatingView.detachFromWindow();
        }
        if (updateNotify) {
            updateNotification();
        }
    }
}
