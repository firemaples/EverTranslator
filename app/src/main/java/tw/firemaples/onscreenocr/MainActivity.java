package tw.firemaples.onscreenocr;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import io.fabric.sdk.android.Fabric;
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler;
import tw.firemaples.onscreenocr.utils.Callback;
import tw.firemaples.onscreenocr.utils.PermissionUtil;
import tw.firemaples.onscreenocr.utils.Tool;

public class MainActivity extends AppCompatActivity {
    public static final String INTENT_START_FROM_NOTIFY = "INTENT_START_FROM_NOTIFY";

    private final int REQUEST_CODE_CHECK_DRAW_OVERLAY_PERM = 101;
    private final int REQUEST_CODE_REQUEST_EXTERNAL_STORAGE_READ_WRITE = 102;
    private final int REQUEST_CODE_REQUEST_MEDIA_PROJECTION_RESULT = 103;

    public static Intent getStarterIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    public static void start(Context context) {
        context.startActivity(getStarterIntent(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        if (!Fabric.isInitialized()) {
            Fabric.with(this, new Crashlytics());
        }
        Answers.getInstance().logCustom(new CustomEvent("App launched"));

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUEST_EXTERNAL_STORAGE_READ_WRITE) {
            onRequestExternalStorageReadWritePermissionResult();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkDrawOverlayPermission() {
        if (!PermissionUtil.checkDrawOverlayPermission(this)) {
            Tool.logInfo("Requesting draw overlay permission");
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setTitle(getString(R.string.dialog_title_needPermission));
            ab.setMessage(getString(R.string.dialog_content_needPermission_drawOverlay));
            ab.setPositiveButton(getString(R.string.btn_setting), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_CODE_CHECK_DRAW_OVERLAY_PERM);
                }
            });
            ab.setNegativeButton(getString(R.string.btn_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.finish();
                }
            });
            ab.show();
        } else {
            Tool.logInfo("Has draw overlay permission");
            checkExternalStorageReadWritePermission();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void onCheckDrawOverlayPermissionResult() {
        if (Settings.canDrawOverlays(this)) {
            Tool.logInfo("Got draw overlay permission");
            checkExternalStorageReadWritePermission();
        } else {
            Tool.logInfo("Not get draw overlay permission, show error dialog");
            showErrorDialog(getString(R.string.dialog_content_needPermission_drawOverlay), new Callback<Void>() {
                @Override
                public boolean onCallback(Void result) {
                    Tool.logInfo("Retry to get draw overlay permission");
                    checkDrawOverlayPermission();
                    return false;
                }
            });
        }
    }

    private void checkExternalStorageReadWritePermission() {
        if (!hasExternalStorageReadWritePermission()) {
            Tool.logInfo("Requesting read/write external storage permission");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_REQUEST_EXTERNAL_STORAGE_READ_WRITE
            );
        } else {
            Tool.logInfo("Has read/write external storage permission");
            requestMediaProjection();
        }
    }

    private boolean hasExternalStorageReadWritePermission() {
//        int writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        int readPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
//
//        return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED;
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void onRequestExternalStorageReadWritePermissionResult() {
        if (hasExternalStorageReadWritePermission()) {
            Tool.logInfo("Got read/write external storage permission");
            requestMediaProjection();
        } else {
            Tool.logInfo("Not get read/write external storage permission");
            showErrorDialog(getString(R.string.dialog_content_needPermission_rwExternalStorage), new Callback<Void>() {
                @Override
                public boolean onCallback(Void result) {
                    Tool.logInfo("Retry to get read/write external storage permission");
                    checkExternalStorageReadWritePermission();
                    return false;
                }
            });
        }
    }

    private void requestMediaProjection() {
        Tool.logInfo("Requesting for media projection");
        try {
//            throw new RuntimeException("Unable to start activity ComponentInfo{tw.firemaples.onscreenocr/tw.firemaples.onscreenocr.MainActivity}: android.content.ActivityNotFoundException: Unable to find explicit activity class {com.android.systemui/com.android.systemui.media.MediaProjectionPermissionActivity}; have you declared this activity in your AndroidManifest.xml?");
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE_REQUEST_MEDIA_PROJECTION_RESULT);
        } catch (NoClassDefFoundError e) {
            String errorMsg = "[MediaProjection] not found";
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                errorMsg += getString(R.string.error_mediaProjectionNotFound_need5UpperVersion);
            } else {
                errorMsg += " with unknown situation";
            }
            showErrorDialog(errorMsg, null);
        } catch (Throwable t) {
            String errorMsg = getString(R.string.error_unknownErrorWhenRequestMediaProjection) + " \r\n\r\n" + t.getLocalizedMessage();
            showErrorDialog(errorMsg, null);
        }
    }

    private void onRequestMediaProjectionResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Tool.logInfo("Got media projection");
            ScreenshotHandler.init(this).setMediaProjectionIntent(data);
            startService();
        } else {
            Tool.logInfo("Not get media projection");
            showErrorDialog(getString(R.string.dialog_content_needPermission_mediaProjection), new Callback<Void>() {
                @Override
                public boolean onCallback(Void result) {
                    Tool.logInfo("Retry to get media projection");
                    requestMediaProjection();
                    return false;
                }
            });
        }
    }


    private void showErrorDialog(String msg, final Callback<Void> onRetryCallback) {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(R.string.dialog_title_error);
        ab.setMessage(msg);
        if (onRetryCallback != null) {
            ab.setPositiveButton(getString(R.string.btn_requestAgain), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onRetryCallback.onCallback(null);
                }
            });
        }
        ab.setNegativeButton(R.string.btn_close, new DialogInterface.OnClickListener() {
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
        boolean fromNotify = false;
        if (getIntent() != null && getIntent().hasExtra(INTENT_START_FROM_NOTIFY)) {
            fromNotify = getIntent().getBooleanExtra(INTENT_START_FROM_NOTIFY, false);
        }
        ScreenTranslatorService.start(this, fromNotify);
        overridePendingTransition(0, 0);
        finish();
    }

    private void stopService() {
        ScreenTranslatorService.stop(true);
    }
}
