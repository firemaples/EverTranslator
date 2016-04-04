package tw.firemaples.onscreenocr.screenshot;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.Tool;

public class ScreenshotPermissionActivity extends AppCompatActivity {

    private static ScreenshotHandler screenshotHandler;
    private final int PERMISSION_CODE = 1234;

    public static Intent getIntent(Context context, ScreenshotHandler screenshotHandler) {
        ScreenshotPermissionActivity.screenshotHandler = screenshotHandler;
        return new Intent(context, ScreenshotPermissionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenshot_permission);

        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_CODE) {
            if (resultCode == RESULT_OK) {
                screenshotHandler.setMediaProjectionIntent(data);
            } else {
                Tool.ShowErrorMsg(this, "Please submit Screenshot Permission for using this service!");
            }

            finish();
        }
    }
}
