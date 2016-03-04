package tw.firemaples.onscreenocr;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.utils.Tool;

public class ScreenCaptureService extends Service implements FloatingViewListener {
    private View floatingView;
    //    private FloatingNotification floatingNotification;
    private FloatingViewManager mFloatingViewManager;
    private CaptureViewHandler captureViewHandler;

    public ScreenCaptureService() {
    }

    public static void start(Context context) {
        if (!Tool.isServiceRunning(context, ScreenCaptureService.class)) {
            context.startService(new Intent(context, ScreenCaptureService.class));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startCommand = super.onStartCommand(intent, flags, startId);

        LayoutInflater inflater = LayoutInflater.from(this);
        floatingView = inflater.inflate(R.layout.widget_floating_button, null, false);
        floatingView.setOnClickListener(onFloatingViewClickListener);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        options.shape = FloatingViewManager.SHAPE_CIRCLE;
        options.overMargin = (int) (16 * metrics.density);
        mFloatingViewManager.addViewToWindow(floatingView, options);

//        floatingNotification = new FloatingNotification(this, floatingView);
//        floatingNotification.setOnFloatingNotificationTapListener(onFloatingNotificationTapListener);
//        floatingNotification.show();

        captureViewHandler = CaptureViewHandler.getInstance(ScreenCaptureService.this);
        captureViewHandler.setCallback(onCaptureViewHandlerCallback);

        ScreenshotHandler.getInstance(this);

        return startCommand;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        floatingNotification.hide();
    }

    private View.OnClickListener onFloatingViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            captureViewHandler.showView();
            floatingView.setVisibility(View.GONE);
        }
    };

    private FloatingNotification.OnFloatingNotificationTapListener onFloatingNotificationTapListener = new FloatingNotification.OnFloatingNotificationTapListener() {
        @Override
        public void onClick(View floatingView) {
            captureViewHandler.showView();
//            floatingNotification.hide();
        }

        @Override
        public void onLongClick(View floatingView) {

        }

        @Override
        public void onDoubleClick(View floatingView) {

        }
    };

    private CaptureViewHandler.OnCaptureViewHandlerCallback onCaptureViewHandlerCallback = new CaptureViewHandler.OnCaptureViewHandlerCallback() {
        boolean tempIsShow = false;

        @Override
        public void onCaptureViewHandlerCloseClick() {
            floatingView.setVisibility(View.VISIBLE);
//            floatingNotification.show();
        }

        @Override
        public void onCaptureScreenStart() {
//            tempIsShow = floatingNotification.isShowing();
//            if (tempIsShow)
//                floatingNotification.hide();

            floatingView.setVisibility(View.GONE);
        }

        @Override
        public void onCaptureScreenEnd() {
//            if (tempIsShow)
//                floatingNotification.show();

            floatingView.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }
}
