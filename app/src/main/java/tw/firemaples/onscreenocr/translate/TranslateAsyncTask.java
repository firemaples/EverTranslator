package tw.firemaples.onscreenocr.translate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import java.util.List;
import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.utils.KeyId;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 2016/3/7.
 */
public class TranslateAsyncTask extends AsyncTask<Void, String, Void> {

    private final Context context;
    private final List<OcrResult> ocrResults;

    private OnTranslateAsyncTaskCallback callback;

    private boolean translate;
    private Language translateFromLang, translateToLang;

    public TranslateAsyncTask(Context context, List<OcrResult> ocrResults, OnTranslateAsyncTaskCallback callback) {
        this.context = context;
        this.ocrResults = ocrResults;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onProgressUpdate(context.getString(R.string.progress_translating));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        translate = preferences.getBoolean(OcrNTranslateUtils.KEY_TRANSLATE, true);

        translateFromLang = OcrNTranslateUtils.getInstance().getTranslateFromLanguage();

        translateToLang = OcrNTranslateUtils.getInstance().getTranslateToLanguage();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Translate.setSubscriptionKey(KeyId.MICROSOFT_TRANSLATE_SUBSCRIPTION_KEY);

        for (OcrResult ocrResult : ocrResults) {
            String ocrText = ocrResult.getText();
            if (translate && ocrText != null && ocrText.length() > 0) {
                try {
                    Answers.getInstance().logCustom(
                            new CustomEvent("Translate Text")
                                    .putCustomAttribute("Text length", ocrText.length())
                                    .putCustomAttribute("Translate from", translateFromLang.name())
                                    .putCustomAttribute("Translate to", translateToLang.name())
                                    .putCustomAttribute("System language", Locale.getDefault().getLanguage()));
                    String translatedText = Translate.execute(ocrText, translateFromLang, translateToLang);
                    ocrResult.setTranslatedText(translatedText);
                } catch (Exception e) {
                    e.printStackTrace();
                    ocrResult.setTranslatedText(context.getString(R.string.error));
                }
            } else {
                ocrResult.setTranslatedText(ocrText);
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        Tool.logInfo(values[0]);
        if (callback != null) {
            callback.showMessage(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (callback != null) {
            callback.hideMessage();
            callback.onTranslateFinished(ocrResults);
        }
    }

    public interface OnTranslateAsyncTaskCallback {
        void onTranslateFinished(List<OcrResult> translatedResult);

        void showMessage(String message);

        void hideMessage();
    }
}
