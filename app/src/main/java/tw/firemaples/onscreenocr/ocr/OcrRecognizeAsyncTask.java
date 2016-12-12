package tw.firemaples.onscreenocr.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 2016/3/2.
 */
public class OcrRecognizeAsyncTask extends AsyncTask<Void, String, List<OcrResult>> {

    private final Context context;
    private final TessBaseAPI baseAPI;
    private Bitmap screenshot;
    private final List<Rect> boxList;

    private static final int textMargin = 10;

    private OnTextRecognizeAsyncTaskCallback callback;

    public OcrRecognizeAsyncTask(Context context, Bitmap screenshot, List<Rect> boxList, OnTextRecognizeAsyncTaskCallback callback) {
        this.context = context;
        this.screenshot = screenshot;
        this.boxList = boxList;
        this.callback = callback;

        this.baseAPI = OcrNTranslateUtils.getInstance().getBaseAPI();
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
        baseAPI.setImage(ReadFile.readBitmap(screenshot));

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        List<OcrResult> ocrResultList = new ArrayList<>();
        for (Rect rect : boxList) {
            baseAPI.setRectangle(rect);
            OcrResult ocrResult = new OcrResult();
            ocrResult.setRect(rect);
            String resultText = baseAPI.getUTF8Text();
            if (Tool.getInstance().removeLineBreaks()) {
                resultText = Tool.replaceAllLineBreaks(resultText, " ");
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

            if (Tool.getInstance().isDebugMode()) {
                OcrResult.DebugInfo debugInfo = new OcrResult.DebugInfo();
                Bitmap cropped = Bitmap.createBitmap(screenshot, rect.left, rect.top, rect.width(), rect.height());
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
        Tool.logInfo("OCR result size:" + results.size());
        if (results.size() > 0) {
            Tool.logInfo("First OCR result:" + results.get(0).getText());
        } else {
            Tool.logInfo("No OCR result found");
            Tool.getInstance().showErrorMsg(context.getString(R.string.error_noOCRResultFound));
        }
        if (callback != null) {
            callback.hideMessage();
            callback.onTextRecognizeFinished(results);
        }
    }

    public interface OnTextRecognizeAsyncTaskCallback {
        void onTextRecognizeFinished(List<OcrResult> results);

        void showMessage(String message);

        void hideMessage();
    }
}
