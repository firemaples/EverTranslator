package tw.firemaples.onscreenocr.translate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import java.util.List;

import tw.firemaples.onscreenocr.SettingsActivity;
import tw.firemaples.onscreenocr.captureview.CaptureView;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.utils.KeyId;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firem_000 on 2016/3/7.
 */
public class TranslateAsyncTask extends AsyncTask<Void, String, Void> {

    private final Context context;
    private final CaptureView captureView;
    private final List<OcrResult> ocrResults;

    private OnTranslateAsyncTaskCallback callback;

    private boolean translate;
    private Language translateFromLang, translateToLang;

    public TranslateAsyncTask(Context context, CaptureView captureView, List<OcrResult> ocrResults) {
        this.context = context;
        this.captureView = captureView;
        this.ocrResults = ocrResults;
    }

    public TranslateAsyncTask setCallback(OnTranslateAsyncTaskCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onProgressUpdate("Starting Translation...");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        translate = preferences.getBoolean(SettingsActivity.KEY_TRANSLATE, true);

        String iso6393From = preferences.getString(SettingsActivity.KEY_RECOGNITION_LANGUAGE, "en");
        String microsoftLangFrom = Tool.mapMicrosoftLanguageCode(iso6393From);
        translateFromLang = Language.fromString(microsoftLangFrom);

        String microsoftLangTo = preferences.getString(SettingsActivity.KEY_TRANSLATION_TO, "en");
        translateToLang = Language.fromString(microsoftLangTo);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Translate.setClientId(KeyId.MICROSOFT_TRANSLATE_CLIENT_ID);
        Translate.setClientSecret(KeyId.MICROSOFT_TRANSLATE_CLIENT_SECRET);

        for (OcrResult ocrResult : ocrResults) {
            if (translate) {
                try {
                    String translatedText = Translate.execute(ocrResult.getText(), translateFromLang, translateToLang);
                    ocrResult.setTranslatedText(translatedText);
                } catch (Exception e) {
                    e.printStackTrace();
                    ocrResult.setTranslatedText("ERROR");
                }
            } else {
                ocrResult.setTranslatedText(ocrResult.getText());
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        Tool.logInfo(values[0]);
        captureView.setProgressMode(true, values[0]);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        captureView.setProgressMode(false, null);
        if (callback != null)
            callback.onTranslateFinished(ocrResults);
    }

    public interface OnTranslateAsyncTaskCallback {
        void onTranslateFinished(List<OcrResult> translatedResult);
    }
}
