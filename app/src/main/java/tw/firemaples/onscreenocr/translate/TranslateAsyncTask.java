package tw.firemaples.onscreenocr.translate;

import android.content.Context;
import android.os.AsyncTask;

import io.github.firemaples.language.Language;
import io.github.firemaples.translate.Translate;
import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.KeyId;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;

/**
 * Created by firemaples on 2016/3/7.
 */
public class TranslateAsyncTask extends AsyncTask<Void, String, String> {

    private final Context context;
    private final String textToTranslate;

    private OnTranslateAsyncTaskCallback callback;

    private Language translateFromLang, translateToLang;

    public TranslateAsyncTask(Context context, String textToTranslate, OnTranslateAsyncTaskCallback callback) {
        this.context = context;
        this.textToTranslate = textToTranslate;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onProgressUpdate(context.getString(R.string.progress_translating));

        translateFromLang = OcrNTranslateUtils.getInstance().getTranslateFromLanguage();

        translateToLang = OcrNTranslateUtils.getInstance().getTranslateToLanguage();
    }

    @Override
    protected String doInBackground(Void... params) {
        Translate.setSubscriptionKey(KeyId.MICROSOFT_TRANSLATE_SUBSCRIPTION_KEY);
        try {
            return Translate.execute(textToTranslate, translateFromLang, translateToLang);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (callback != null) {
            callback.onTranslateFinished(result);
        }
    }

    public interface OnTranslateAsyncTaskCallback {
        void onTranslateFinished(String translatedText);
    }
}
