package tw.firemaples.onscreenocr;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.utils.Callback;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE_CHECK_DRAW_OVERLAY_PERM = 1001;
    private final int REQUEST_CODE_REQUEST_MEDIA_PROJECTION_RESULT = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        findViewById(R.id.bt_serviceToggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OnScreenTranslateService.isRunning(MainActivity.this)) {
                    stopService();
                } else {
                    startApp();
                }
            }
        });

        //noinspection ConstantConditions
        findViewById(R.id.bt_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.start(MainActivity.this, null);
            }
        });

        startApp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CHECK_DRAW_OVERLAY_PERM) {
            onCheckDrawOverlayPermissionResult();
        } else if (requestCode == REQUEST_CODE_REQUEST_MEDIA_PROJECTION_RESULT) {
            onRequestMediaProjectionResult(resultCode, data);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(MainActivity.this)) {
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setTitle("Need permission");
            ab.setMessage("This app needs [DrawOverlay] Permission for running!");
            ab.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_CODE_CHECK_DRAW_OVERLAY_PERM);
                }
            });
            ab.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.finish();
                }
            });
            ab.show();
        } else {
            requestMediaProjection();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void onCheckDrawOverlayPermissionResult() {
        if (Settings.canDrawOverlays(this)) {
            requestMediaProjection();
        } else {
            showErrorDialog("This app needs [DrawOverlay] Permission for running!", new Callback<Void>() {
                @Override
                public boolean onCallback(Void result) {
                    requestMediaProjection();
                    return false;
                }
            });
        }
    }

    private void requestMediaProjection() {
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE_REQUEST_MEDIA_PROJECTION_RESULT);
    }

    private void onRequestMediaProjectionResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ScreenshotHandler.init(this).setMediaProjectionIntent(data);
            startService();
        } else {
            showErrorDialog("Please submit Screenshot Permission for using this service!", new Callback<Void>() {
                @Override
                public boolean onCallback(Void result) {
                    requestMediaProjection();
                    return false;
                }
            });
        }
    }

    private void showErrorDialog(String msg, final Callback<Void> onRetryCallback) {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("Error");
        ab.setMessage(msg);
        ab.setPositiveButton("Request Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onRetryCallback.onCallback(null);
            }
        });
        ab.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        ab.show();
    }

    private void startApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkDrawOverlayPermission();
        } else {
            requestMediaProjection();
        }
    }

    private void startService() {
//        OnScreenTranslateService.start(this);
        ScreenTranslatorService.start(this);
        finish();
    }

    private void stopService() {
//        OnScreenTranslateService.stop();
        ScreenTranslatorService.stop();
    }
}
