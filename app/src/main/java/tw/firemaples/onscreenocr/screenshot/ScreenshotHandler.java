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
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tw.firemaples.onscreenocr.MainActivity;
import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.log.FirebaseEvent;
import tw.firemaples.onscreenocr.utils.ImageFile;
import tw.firemaples.onscreenocr.utils.NotchUtil;
import tw.firemaples.onscreenocr.utils.SettingUtil;
import tw.firemaples.onscreenocr.utils.Utils;

/**
 * Created by firemaples on 2016/3/4.
 */
public class ScreenshotHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotHandler.class);

    private static ScreenshotHandler _instance;
    public final static int TIMEOUT = 5000;

    public final static int ERROR_CODE_KNOWN_ERROR = 0;
    public final static int ERROR_CODE_TIMEOUT = 1;
    public final static int ERROR_CODE_IMAGE_FORMAT_ERROR = 2;
    public final static int ERROR_CODE_OUT_OF_MEMORY = 3;
    public final static int ERROR_CODE_IO_EXCEPTION = 4;

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
        Utils.showToast(context.getString(R.string.error_noMediaProjectionFound));
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

    public void takeScreenshot(long delay) {
        if (callback != null) {
            callback.onScreenshotStart();
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                _takeScreenshot();
            }
        }, delay);
    }

    public void takeScreenshot() {
        if (callback != null) {
            callback.onScreenshotStart();
        }

        _takeScreenshot();
    }

    private void _takeScreenshot() {
        logger.info("Start screenshot");
        final long screenshotStartTime = System.currentTimeMillis();

        final File screenshotFile = new File(context.getCacheDir(), "screenshot.jpg");
        if (screenshotFile.exists() && !screenshotFile.delete()) {
            callback.onScreenshotFailed(ERROR_CODE_IO_EXCEPTION, new IOException("Delete the old one screenshot failed"));
            return;
        }

        final MediaProjection mProjection = getMediaProjection();
        if (mProjection == null) {
            logger.error("MediaProjection is null");
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
        final boolean isPortrait = mHeight > mWidth;

        //Create a imageReader for catch result
        final ImageReader mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);

        final Handler handler = new Handler();

        //Take a screenshot
        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
        mProjection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, flags, mImageReader.getSurface(), null, handler);

        //convert result into image
        logger.info("add setOnImageAvailableListener");
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                mImageReader.setOnImageAvailableListener(null, handler);
                mImageReader.close();
                mProjection.stop();

                logger.error("Screenshot timeout");
                if (callback != null) {
                    callback.onScreenshotFailed(ERROR_CODE_TIMEOUT, null);
                }
            }
        };
        handler.postDelayed(timeoutRunnable, TIMEOUT);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                reader.setOnImageAvailableListener(null, handler);
                logger.info("onImageAvailable");
                Image image = null;
                Bitmap tempBmp = null;
                Bitmap realSizeBitmap = null;
                int[] size = null;
                Throwable error = null;
                int errorCode = ERROR_CODE_KNOWN_ERROR;
                try {
                    image = reader.acquireLatestImage();
//                    throw new UnsupportedOperationException("The producer output buffer format 0x5 doesn't match the ImageReader's configured buffer format 0x1.");
                    logger.info("screenshot image info: width:" + image.getWidth() + " height:" + image.getHeight());
                    int deviceWidth = metrics.widthPixels;
                    int deviceHeight = metrics.heightPixels;
                    if (deviceHeight > deviceWidth != isPortrait) {
                        logger.warn("Height & width ratio is not match orientation, swap height & width");
                        //noinspection SuspiciousNameCombination
                        deviceWidth = metrics.heightPixels;
                        //noinspection SuspiciousNameCombination
                        deviceHeight = metrics.widthPixels;
                    }

                    //The real size with the notch
                    int notchWidthDiff = 0;
                    int notchHeightDiff = 0;
                    NotchUtil notchUtil = NotchUtil.INSTANCE;
                    if (notchUtil.getHasNotch()) {
                        if (isPortrait) {
                            notchHeightDiff = notchUtil.getStatusBarHeight();
                        } else {
                            notchWidthDiff = notchUtil.getNotchHeight();
                        }
                    }

                    final Image.Plane[] planes = image.getPlanes();
                    final ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * deviceWidth;
                    // create bitmap
                    tempBmp = Bitmap.createBitmap(
                            deviceWidth + (int) ((float) rowPadding / (float) pixelStride),
                            deviceHeight + notchHeightDiff, Bitmap.Config.ARGB_8888);
                    tempBmp.copyPixelsFromBuffer(buffer);

                    realSizeBitmap = Bitmap.createBitmap(tempBmp, notchWidthDiff, notchHeightDiff,
                            deviceWidth, tempBmp.getHeight() - notchHeightDiff);

                    size = new int[]{realSizeBitmap.getWidth(), realSizeBitmap.getHeight()};
                    saveBmpToFile(realSizeBitmap, screenshotFile);

                    if (SettingUtil.INSTANCE.isDebugMode()) {
                        String fileName = String.format(Locale.getDefault(), "debug_screenshot_%s.jpg",
                                new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault())
                                        .format(new Date(System.currentTimeMillis())));
                        File debugFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
                        Utils.showToast("Saving debug screenshot to " + debugFile.getAbsolutePath());
                        saveBmpToFile(realSizeBitmap, debugFile);
                    }
                } catch (Throwable e) {
                    logger.error("Screenshot failed", e);
                    error = e;
                    if (callback != null) {
                        if (e instanceof UnsupportedOperationException) {
                            errorCode = ERROR_CODE_IMAGE_FORMAT_ERROR;
                        } else if (e instanceof FileNotFoundException) {
                            errorCode = ERROR_CODE_IO_EXCEPTION;
                        } else if (e.getMessage() != null) {
                            String errorMsg = e.getMessage();
                            if (errorMsg.contains("Buffer not large enough for pixels")) {
                                errorCode = ERROR_CODE_OUT_OF_MEMORY;
                            }
                        }
                    }
                    FirebaseEvent.INSTANCE.logException(e);
                } finally {
                    if (image != null) {
                        image.close();
                    }
                    try {
                        reader.close();
                    } catch (Exception e) {
                        logger.error("Screenshot failed", e);
                        error = e;
                        if (e.getMessage() != null && e.getMessage().contains("Attempted to free")) {
                            errorCode = ERROR_CODE_OUT_OF_MEMORY;
                        }
                        FirebaseEvent.INSTANCE.logException(e);
                    } finally {
                        mProjection.stop();
                        if (tempBmp != null) {
                            tempBmp.recycle();
                        }
                        if (realSizeBitmap != null) {
                            realSizeBitmap.recycle();
                        }
                    }
                }

                if (timeoutRunnable != null) {
                    handler.removeCallbacks(timeoutRunnable);
                    timeoutRunnable = null;
                }
                if (error == null && size != null && screenshotFile.exists()) {
                    long spentTime = System.currentTimeMillis() - screenshotStartTime;
                    logger.info("Screenshot finished, spent: " + spentTime + " ms");
                    if (callback != null) {
                        callback.onScreenshotFinished(new ImageFile(screenshotFile, size[0], size[1]));
                    }
                } else {
                    callback.onScreenshotFailed(errorCode, error);
                }
            }
        }, handler);
    }

    private void saveBmpToFile(Bitmap bitmap, File file) throws FileNotFoundException {
        logger.info("Saving debug screenshot to " + file.getAbsolutePath());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (FileNotFoundException e) {
            logger.error("Save debug screenshot failed");
            throw e;
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
        void onScreenshotStart();

        void onScreenshotFinished(ImageFile screenshotFile);

        void onScreenshotFailed(int errorCode, @Nullable Throwable e);
    }
}
