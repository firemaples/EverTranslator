package tw.firemaples.onscreenocr;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;

import tw.firemaples.onscreenocr.utils.Tool;

public class ScreenCaptureService extends Service {
    private FloatingNotification floatingNotification;
    private View floatingView;
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
        floatingNotification = new FloatingNotification(this, floatingView);
        floatingNotification.setOnFloatingNotificationTapListener(onFloatingNotificationTapListener);
        floatingNotification.show();

        captureViewHandler = CaptureViewHandler.getInstance(ScreenCaptureService.this);
        captureViewHandler.setCallback(onCaptureViewHandlerCallback);

        return startCommand;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        floatingNotification.hide();
    }

    private FloatingNotification.OnFloatingNotificationTapListener onFloatingNotificationTapListener = new FloatingNotification.OnFloatingNotificationTapListener() {
        @Override
        public void onClick(View floatingView) {
            captureViewHandler.showView();
            floatingNotification.hide();
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
            floatingNotification.show();
        }

        @Override
        public void onCaptureScreenStart() {
            tempIsShow = floatingNotification.isShowing();
            if (tempIsShow)
                floatingNotification.hide();
        }

        @Override
        public void onCaptureScreenEnd() {
            if(tempIsShow)
                floatingNotification.show();
        }
    };
}
