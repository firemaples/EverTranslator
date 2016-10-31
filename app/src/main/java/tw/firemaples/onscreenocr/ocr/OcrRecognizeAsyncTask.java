package tw.firemaples.onscreenocr.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.utils.OcrUtils;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firem_000 on 2016/3/2.
 */
public class OcrRecognizeAsyncTask extends AsyncTask<Void, String, List<OcrResult>> {

    private final Context context;
    private final TessBaseAPI baseAPI;
    private Bitmap screenshot;
    private final List<Rect> boxList;

    private final int textMargin = 10;

    private OnTextRecognizeAsyncTaskCallback callback;

    public OcrRecognizeAsyncTask(Context context, Bitmap screenshot, List<Rect> boxList, OnTextRecognizeAsyncTaskCallback callback) {
        this.context = context;
        this.screenshot = screenshot;
        this.boxList = boxList;
        this.callback = callback;

        this.baseAPI = OcrUtils.getInstance().getBaseAPI();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onProgressUpdate("Orc recognizing...");

        if (callback == null) {
            throw new UnsupportedOperationException("Callback is not implemented");
        }
    }

    @Override
    protected List<OcrResult> doInBackground(Void... params) {
        baseAPI.setImage(ReadFile.readBitmap(screenshot));
        List<OcrResult> ocrResultList = new ArrayList<>();
        for (Rect rect : boxList) {
            baseAPI.setRectangle(rect);
            OcrResult ocrResult = new OcrResult();
            ocrResult.setRect(rect);
            ocrResult.setText(baseAPI.getUTF8Text());
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
        Tool.logInfo("Orc result size:" + results.size());
        if (results.size() > 0) {
            Tool.logInfo("First orc result:" + results.get(0).getText());
        } else {
            Tool.logInfo("No orc result found");
            Tool.showErrorMsg("No orc result found");
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
