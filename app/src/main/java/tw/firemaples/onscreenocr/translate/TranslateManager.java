package tw.firemaples.onscreenocr.translate;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.Locale;

import tw.firemaples.onscreenocr.database.DatabaseManager;
import tw.firemaples.onscreenocr.database.TranslateServiceModel;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 01/05/2017.
 */

public class TranslateManager {
    private static TranslateManager _instance;

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private TranslateManager() {
    }

    public static TranslateManager getInstance() {
        if (_instance == null) {
            _instance = new TranslateManager();
        }

        return _instance;
    }

    public void startTranslate(Context context, String text, final OnTranslateManagerCallback callback) {
        if (text == null || text.trim().length() == 0 || callback == null) {
            return;
        }
        if (!SharePreferenceUtil.getInstance().isEnableTranslation()) {
            callback.onTranslateFinished(text);
            return;
        }

        TranslateServiceModel translateService = DatabaseManager.getInstance().getTranslateService();
        String translateFromLang = OcrNTranslateUtils.getInstance().getTranslateFromLang();
        String translateToLang = OcrNTranslateUtils.getInstance().getTranslateToLang();
        Tool.logInfo("Translate with " + translateService.getCurrent().name());
        switch (translateService.getCurrent()) {
            case google: {
                GoogleTranslateWebView googleTranslateWebView = new GoogleTranslateWebView(context);
                googleTranslateWebView.startTranslate(text, translateToLang, new GoogleTranslateWebView.OnGoogleTranslateWebViewCallback() {
                    @Override
                    public void onTranslated(final String translatedText) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onTranslateFinished(translatedText);
                            }
                        });
                    }
                });
            }
            break;
            case microsoft: {
                new TranslateAsyncTask(context, text, new TranslateAsyncTask.OnTranslateAsyncTaskCallback() {
                    @Override
                    public void onTranslateFinished(final String translatedText) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onTranslateFinished(translatedText);
                            }
                        });
                    }
                }).execute();
            }
            break;
        }
        Answers.getInstance().logCustom(
                new CustomEvent("Translate Text")
                        .putCustomAttribute("Text length", text.length())
                        .putCustomAttribute("Translate from", translateFromLang)
                        .putCustomAttribute("Translate to", translateToLang)
                        .putCustomAttribute("From > to", translateFromLang + " > " + translateToLang)
                        .putCustomAttribute("System language", Locale.getDefault().getLanguage())
                        .putCustomAttribute("Translate service", translateService.getCurrent().name())
        );
    }

    public interface OnTranslateManagerCallback {
        void onTranslateFinished(String translatedText);
    }
}
