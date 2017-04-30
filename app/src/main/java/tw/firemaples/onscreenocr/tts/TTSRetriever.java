package tw.firemaples.onscreenocr.tts;

import android.os.AsyncTask;

import java.io.File;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class TTSRetriever extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {

        return null;
    }

    public interface OnTTSRetrieverCallBack {
        void onSuccess(File ttsFile);

        void onFailed();
    }
}
