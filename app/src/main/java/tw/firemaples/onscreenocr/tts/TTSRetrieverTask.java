package tw.firemaples.onscreenocr.tts;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class TTSRetrieverTask extends AsyncTask<Void, Void, File> {
    private TTSManager ttsManager;

    private final Context context;
    private final String lang;
    private final String ttsContent;

    private final OnTTSRetrieverCallback callback;

    public TTSRetrieverTask(Context context, String lang, String ttsContent, OnTTSRetrieverCallback callback) {
        this.context = context;
        this.lang = lang;
        this.ttsContent = ttsContent;
        this.callback = callback;

        ttsManager = AndroidTTSManager.getInstance(context);
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            return ttsManager.retrieveTTSFile(lang, ttsContent);
        } catch (AndroidTTSManager.LanguageNotSupportException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if (file != null) {
            callback.onSuccess(file);
        } else {
            callback.onFailed();
        }
    }

    public interface OnTTSRetrieverCallback {
        void onSuccess(File ttsFile);

        void onFailed();
    }
}
