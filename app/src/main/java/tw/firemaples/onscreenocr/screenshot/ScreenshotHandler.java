package tw.firemaples.onscreenocr.screenshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tw.firemaples.onscreenocr.MainActivity;
import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firem_000 on 2016/3/4.
 */
public class ScreenshotHandler {
    private static ScreenshotHandler _instance;
    private final static int TIMEOUT = 5000;

    public final static int ERROR_CODE_TIMEOUT = 1;

    private Context context;
    private boolean isGetUserPermission;
    private Intent mediaProjectionIntent;
    private OnScreenshotHandlerCallback callback;
    private Runnable timeoutRunnable;

    private ScreenshotHandler(Context context) {
        this.context = context;
    }

    private ScreenshotHandler() {
    }

    public static ScreenshotHandler init(Context context) {
        _instance = new ScreenshotHandler(context);
        return _instance;
    }

    public static boolean isInitialized() {
        return _instance != null && _instance.isGetUserPermission();
    }

    public static ScreenshotHandler getInstance() {
        if (_instance == null) {
            _instance = new ScreenshotHandler();
        }
        return _instance;
    }

    public void setCallback(OnScreenshotHandlerCallback callback) {
        this.callback = callback;
    }

    public void release() {
        _instance = null;
    }

    public boolean isGetUserPermission() {
        return isGetUserPermission && mediaProjectionIntent != null;
    }

    public void getUserPermission() {
        if (isGetUserPermission) {
            return;
        }
        Tool.getInstance().showMsg(context.getString(R.string.error_noMediaProjectionFound));
        context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
            Tool.logError("MediaProjection is null");
            return;
        }
        // http://binwaheed.blogspot.tw/2015/03/how-to-correctly-take-screenshot-using.html
        // Get size of screen
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Point size = new Point();
        display.getRealSize(size);
        final int mWidth = size.x;
        final int mHeight = size.y;
        int mDensity = metrics.densityDpi;

        //Create a imageReader for catch result
        final ImageReader mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);

        final Handler handler = new Handler();

        //Take a screenshot
        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
        mProjection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, flags, mImageReader.getSurface(), null, handler);

        //convert result into image
        Tool.logInfo("add setOnImageAvailableListener");
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                mImageReader.setOnImageAvailableListener(null, handler);
                mImageReader.close();
                mProjection.stop();

                if (callback != null) {
                    callback.onScreenshotFailed(ERROR_CODE_TIMEOUT);
                }
            }
        };
        handler.postDelayed(timeoutRunnable, TIMEOUT);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                reader.setOnImageAvailableListener(null, handler);
                Tool.logInfo("onImageAvailable");
                Image image = reader.acquireLatestImage();
                Tool.logInfo("screenshot image info: width:" + image.getWidth() + " height:" + image.getHeight());
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();
//                int offset = 0;
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * metrics.widthPixels;
                // create bitmap
                Bitmap bmp = Bitmap.createBitmap(metrics.widthPixels + (int) ((float) rowPadding / (float) pixelStride), metrics.heightPixels, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);

                image.close();
                reader.close();
                mProjection.stop();

                Bitmap realSizeBitmap = Bitmap.createBitmap(bmp, 0, 0, metrics.widthPixels, bmp.getHeight());
                bmp.recycle();

                if (Tool.getInstance().isDebugMode()) {
                    saveBmpToFile(realSizeBitmap);
                }

                if (timeoutRunnable != null) {
                    handler.removeCallbacks(timeoutRunnable);
                    timeoutRunnable = null;
                }
                if (callback != null) {
                    callback.onScreenshotFinished(realSizeBitmap);
                }
            }
        }, handler);
    }

    private void saveBmpToFile(Bitmap bitmap) {
        String fileName = String.format(Locale.getDefault(), "debug_screenshot_%s.jpg",
                new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault())
                        .format(new Date(System.currentTimeMillis())));
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
        Tool.logInfo("Saving debug screenshot to " + file.getAbsolutePath());
        Tool.getInstance().showMsg("Saving debug screenshot to " + file.getAbsolutePath());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            Tool.logError("Save debug screenshot failed");
            Tool.getInstance().showErrorMsg("Save debug screenshot failed");
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnScreenshotHandlerCallback {
        void onScreenshotFinished(Bitmap bitmap);

        void onScreenshotFailed(int errorCode);
    }
}
