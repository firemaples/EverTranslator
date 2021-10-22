package tw.firemaples.onscreenocr.ocr.tesseract;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.ImageFile;
import tw.firemaples.onscreenocr.utils.SettingUtil;
import tw.firemaples.onscreenocr.utils.Utils;

/**
 * Created by firemaples on 2016/3/2.
 */
public class OcrRecognizeAsyncTask extends AsyncTask<Void, String, List<OcrResult>> {
    private static final Logger logger = LoggerFactory.getLogger(OcrRecognizeAsyncTask.class);

    private final Context context;
    private final TessBaseAPI baseAPI;
    private ImageFile screenshot;
    private final List<Rect> boxList;

    private static final int textMargin = 10;

    private OnTextRecognizeAsyncTaskCallback callback;

    public OcrRecognizeAsyncTask(Context context, ImageFile screenshot, List<Rect> boxList, OnTextRecognizeAsyncTaskCallback callback) {
        this.context = context;
        this.screenshot = screenshot;
        this.boxList = boxList;
        this.callback = callback;

        this.baseAPI = TesseractOCRManager.INSTANCE.getTessBaseAPI();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onProgressUpdate(context.getString(R.string.progress_textRecognizing));

        if (callback == null) {
            throw new UnsupportedOperationException("Callback is not implemented");
        }
    }

    @Override
    protected List<OcrResult> doInBackground(Void... params) {
        baseAPI.setImage(ReadFile.readFile(screenshot.getFile()));

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        List<OcrResult> ocrResultList = new ArrayList<>();
        for (Rect rect : boxList) {
            //Try to fix sides of rect
            fixRect(rect, screenshot.getWidth(), screenshot.getHeight());

            baseAPI.setRectangle(rect);
            OcrResult ocrResult = new OcrResult();
            ocrResult.setRect(rect);
            String resultText = baseAPI.getUTF8Text();
            if (SettingUtil.INSTANCE.getRemoveLineBreaks()) {
                resultText = Utils.replaceAllLineBreaks(resultText, " ");
            }
            ocrResult.setText(resultText);
            ocrResult.setBoxRects(baseAPI.getRegions().getBoxRects());
            ocrResult.setResultIterator(baseAPI.getResultIterator());

            if (ocrResult.getBoxRects().size() > 0) {
                Rect boxRect = ocrResult.getBoxRects().get(0);

                Rect subRect = new Rect(rect.left + boxRect.left - textMargin,
                        rect.top + boxRect.top - textMargin,
                        rect.left + boxRect.right + textMargin,
                        rect.top + boxRect.bottom + textMargin);
                ocrResult.setSubRect(subRect);
            }

            if (SettingUtil.INSTANCE.isDebugMode()) {
                OcrResult.DebugInfo debugInfo = new OcrResult.DebugInfo();
                Bitmap fullBitmap = BitmapFactory.decodeFile(screenshot.getFile().getAbsolutePath());
                Bitmap cropped = Bitmap.createBitmap(fullBitmap, rect.left, rect.top, rect.width(), rect.height());
                fullBitmap.recycle();
                debugInfo.setCroppedBitmap(cropped);
                debugInfo.addInfoString(String.format(Locale.getDefault(), "Screen size:%dx%d", metrics.widthPixels, metrics.heightPixels));
                debugInfo.addInfoString(String.format(Locale.getDefault(), "Screenshot size:%dx%d", screenshot.getWidth(), screenshot.getHeight()));
                debugInfo.addInfoString(String.format(Locale.getDefault(), "Cropped position:%s", rect.toString()));
                debugInfo.addInfoString(String.format(Locale.getDefault(), "OCR result:%s", ocrResult.getText()));
                ocrResult.setDebugInfo(debugInfo);
            }

            ocrResultList.add(ocrResult);
        }

        return ocrResultList;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (callback != null) {
            callback.showMessage(values[0]);
        }
    }

    @Override
    protected void onPostExecute(List<OcrResult> results) {
        super.onPostExecute(results);
        logger.info("OCR result size:" + results.size());
        if (results.size() > 0) {
            logger.info("First OCR result:" + results.get(0).getText());
        } else {
            logger.info("No OCR result found");
            Utils.showErrorToast(context.getString(R.string.error_noOCRResultFound));
        }
        if (callback != null) {
            callback.hideMessage();
            callback.onTextRecognizeFinished(results);
        }
    }

    private void fixRect(Rect rect, int bitmapWidth, int bitmapHeight) {
        if (rect.left < 0) {
            rect.left = 0;
        }
        if (rect.top < 0) {
            rect.top = 0;
        }
        if (rect.right > bitmapWidth) {
            rect.right = bitmapWidth;
        }
        if (rect.bottom > bitmapHeight) {
            rect.bottom = bitmapHeight;
        }
    }

    public interface OnTextRecognizeAsyncTaskCallback {
        void onTextRecognizeFinished(List<OcrResult> results);

        void showMessage(String message);

        void hideMessage();
    }
}
