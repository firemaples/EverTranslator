package tw.firemaples.onscreenocr.screenshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.nio.ByteBuffer;

import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firem_000 on 2016/3/4.
 */
public class ScreenshotHandler {
    private Context context;
    private static ScreenshotHandler screenshotHandler;
    private boolean isGetUserPermission;
    private Intent mediaProjectionIntent;
    private OnScreenshotHandlerCallback callback;

    private ScreenshotHandler(Context context) {
        this.context = context;

        getUserPermission();
    }

    public static ScreenshotHandler getInstance(Context context) {
        if (screenshotHandler == null) screenshotHandler = new ScreenshotHandler(context);
        return screenshotHandler;
    }

    public void setCallback(OnScreenshotHandlerCallback callback) {
        this.callback = callback;
    }

    public void release() {
        screenshotHandler = null;
    }

    public boolean isGetUserPermission() {
        return isGetUserPermission;
    }

    public void getUserPermission() {
        if (isGetUserPermission) return;
        Tool.ShowMsg(context, "Please submit Screenshot Permission for using this service!");
        context.startActivity(ScreenshotPermissionActivity.getIntent(context, this).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private MediaProjection getMediaProjection() {
        if (!isGetUserPermission) {
            getUserPermission();
            return null;
        } else {
            MediaProjectionManager projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            return projectionManager.getMediaProjection(Activity.RESULT_OK, (Intent) mediaProjectionIntent.clone());
        }
    }

    public void setMediaProjectionIntent(Intent mediaProjectionIntent) {
        this.mediaProjectionIntent = (Intent) mediaProjectionIntent.clone();
        isGetUserPermission = true;
    }

    public void takeScreenshot() {
        final MediaProjection mProjection = getMediaProjection();
        if (mProjection == null) {
            Tool.LogError("MediaProjection is null");
            return;
        }
        // http://binwaheed.blogspot.tw/2015/03/how-to-correctly-take-screenshot-using.html
        // Get size of screen
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        final int mWidth = metrics.widthPixels;
        final int mHeight = metrics.heightPixels;
        int mDensity = metrics.densityDpi;

        //Create a imageReader for catch result
//        final ImageReader mImageReader = ImageReader.newInstance(mWidth, mHeight, ImageFormat.RGB_565, 2);
        ImageReader mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);

        //Take a screenshot
        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
        mProjection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, flags, mImageReader.getSurface(), null, null);

        //convert result into image
        final Handler handler = new Handler();
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                reader.setOnImageAvailableListener(null, handler);
                Image image = reader.acquireLatestImage();
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();
//                int offset = 0;
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * metrics.widthPixels;
                // create bitmap
//                Bitmap bmp = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.RGB_565);
                Bitmap bmp = Bitmap.createBitmap(metrics.widthPixels + rowPadding / pixelStride, metrics.heightPixels, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);
                image.close();

                reader.close();
                mProjection.stop();

                if (callback != null)
                    callback.onScreenshotFinished(bmp);
            }
        }, handler);
    }

    public interface OnScreenshotHandlerCallback {
        void onScreenshotFinished(Bitmap bitmap);
    }
}
