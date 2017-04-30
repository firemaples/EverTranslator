package tw.firemaples.onscreenocr.tts;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class TTSRetrieverTask extends AsyncTask<Void, Void, File> {
    private AndroidTTSManager ttsManager;

    private static int staticRequestId = 0;

    private final int requestId;
    private final Context context;
    private final String lang;
    private final String ttsContent;

    private final OnTTSRetrieverCallback callback;

    public TTSRetrieverTask(Context context, String lang, String ttsContent, OnTTSRetrieverCallback callback) {
        this.context = context;
        this.lang = lang;
        this.ttsContent = ttsContent;
        this.callback = callback;
        requestId = ++staticRequestId;

        ttsManager = AndroidTTSManager.getInstance(context);
        ttsManager.setCallback(String.valueOf(requestId), androidTTSManagerCallback);
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            ttsManager.retrieveTTSFile(lang, ttsContent, String.valueOf(requestId));
        } catch (AndroidTTSManager.LanguageNotSupportException e) {
            e.printStackTrace();
        }
        return null;
    }

    private AndroidTTSManager.AndroidTTSManagerCallback androidTTSManagerCallback = new AndroidTTSManager.AndroidTTSManagerCallback() {
        @Override
        public void onDone(File file) {
            callback.onSuccess(file);
        }

        @Override
        public void onError() {
            callback.onFailed();
        }
    };

    public interface OnTTSRetrieverCallback {
        void onSuccess(File ttsFile);

        void onFailed();
    }
}
