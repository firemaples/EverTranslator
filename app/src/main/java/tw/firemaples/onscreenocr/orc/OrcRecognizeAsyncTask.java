package tw.firemaples.onscreenocr.orc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.captureview.CaptureView;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firem_000 on 2016/3/2.
 */
public class OrcRecognizeAsyncTask extends AsyncTask<Void, String, List<OrcResult>> {

    private final Context context;
    private final TessBaseAPI baseAPI;
    private final tw.firemaples.onscreenocr.captureview.CaptureView CaptureView;
    private Bitmap screenshot;
    private final List<Rect> boxList;

    private final int textMargin = 10;

    private OnTextRecognizeAsyncTaskCallback callback;

    public OrcRecognizeAsyncTask(Context context, TessBaseAPI baseAPI, CaptureView CaptureView, Bitmap screenshot, List<Rect> boxList) {
        this.context = context;
        this.baseAPI = baseAPI;
        this.CaptureView = CaptureView;
        this.screenshot = screenshot;
        this.boxList = boxList;
    }

    public OrcRecognizeAsyncTask setCallback(OnTextRecognizeAsyncTaskCallback callback) {
        this.callback = callback;
        return this;
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
    protected List<OrcResult> doInBackground(Void... params) {
        baseAPI.setImage(ReadFile.readBitmap(screenshot));
        List<OrcResult> orcResultList = new ArrayList<>();
        for (Rect rect : boxList) {
            baseAPI.setRectangle(rect);
            OrcResult orcResult = new OrcResult();
            orcResult.setRect(rect);
            orcResult.setText(baseAPI.getUTF8Text());
            orcResult.setBoxRects(baseAPI.getRegions().getBoxRects());
            orcResult.setResultIterator(baseAPI.getResultIterator());

            if (orcResult.getBoxRects().size() > 0) {
                Rect boxRect = orcResult.getBoxRects().get(0);

                Rect subRect = new Rect(rect.left + boxRect.left - textMargin,
                        rect.top + boxRect.top - textMargin,
                        rect.left + boxRect.right + textMargin,
                        rect.top + boxRect.bottom + textMargin);
                orcResult.setSubRect(subRect);
            }

            orcResultList.add(orcResult);
        }

        return orcResultList;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        CaptureView.setProgressMode(true, values[0]);
    }

    @Override
    protected void onPostExecute(List<OrcResult> results) {
        super.onPostExecute(results);
        Tool.logInfo("Orc result size:" + results.size());
        if (results.size() > 0) {
            Tool.logInfo("First orc result:" + results.get(0).getText());
        } else {
            Tool.logInfo("No orc result found");
            Tool.showErrorMsg("No orc result found");
        }
        CaptureView.setProgressMode(false, null);
        if (callback != null) {
            callback.onTextRecognizeFinished(results);
        }
    }

    public interface OnTextRecognizeAsyncTaskCallback {
        void onTextRecognizeFinished(List<OrcResult> results);
    }
}
