package tw.firemaples.onscreenocr.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;

import java.util.List;

public class OCRManager {
    private static OCRManager _instance;

    private Context context;

    private OnOCRStateChangedListener callback;
    private AsyncTask lastAsyncTask;

    private Bitmap currentScreenshot;
    private List<Rect> boxList;

    public static synchronized OCRManager getInstance(Context context) {
        if (_instance == null) {
            _instance = new OCRManager(context);
        }

        return _instance;
    }

    private OCRManager(Context context) {
        this.context = context;
    }

    public void setListener(OnOCRStateChangedListener callback) {
        this.callback = callback;
    }

    public void start(Bitmap screenshot, List<Rect> boxList) {
        this.currentScreenshot = screenshot;
        this.boxList = boxList;

        initOcrEngine();
    }

    public void cancelRunningTask() {
        if (lastAsyncTask != null) {
            lastAsyncTask.cancel(true);
        }
    }

    private void initOcrEngine() {
        if (callback != null) {
            callback.onInitializing();
        }

        lastAsyncTask = new OcrInitAsyncTask(context, onOcrInitAsyncTaskCallback).execute();
    }

    private OcrInitAsyncTask.OnOcrInitAsyncTaskCallback onOcrInitAsyncTaskCallback =
            new OcrInitAsyncTask.OnOcrInitAsyncTaskCallback() {
                @Override
                public void onOcrInitialized() {
                    if (callback != null) {
                        callback.onInitialized();
                    }
                    startTextRecognize();
                }

                @Override
                public void showMessage(String message) {
                }

                @Override
                public void hideMessage() {
                }
            };

    private void startTextRecognize() {
        if (callback != null) {
            callback.onRecognizing();
        }

        lastAsyncTask = new OcrRecognizeAsyncTask(context,
                currentScreenshot,
                boxList,
                onTextRecognizeAsyncTaskCallback).execute();
    }

    private OcrRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback onTextRecognizeAsyncTaskCallback =
            new OcrRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback() {
                @Override
                public void onTextRecognizeFinished(List<OcrResult> results) {
                    if (callback != null) {
                        callback.onRecognized(results);
                    }
                }

                @Override
                public void showMessage(String message) {
                }

                @Override
                public void hideMessage() {
                }
            };


    public interface OnOCRStateChangedListener {
        void onInitializing();

        void onInitialized();

        void onRecognizing();

        void onRecognized(List<OcrResult> results);
    }
}
