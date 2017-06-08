package tw.firemaples.onscreenocr.translate;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import tw.firemaples.onscreenocr.database.DatabaseManager;
import tw.firemaples.onscreenocr.database.TranslateServiceModel;
import tw.firemaples.onscreenocr.utils.FabricUtil;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 01/05/2017.
 */

public class TranslateManager {
    private static TranslateManager _instance;

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    GoogleTranslateWebView googleTranslateWebView;

    private TranslateManager() {
    }

    public static TranslateManager getInstance() {
        if (_instance == null) {
            _instance = new TranslateManager();
        }

        return _instance;
    }

    public void startTranslate(Context context, String text, OnTranslateManagerCallback callback) {
        if (text == null || text.trim().length() == 0 || callback == null) {
            return;
        }
        if (!SharePreferenceUtil.getInstance().isEnableTranslation()) {
            callback.onTranslateFinished(text);
            return;
        }

        TranslateServiceModel translateService = DatabaseManager.getInstance().getTranslateService();

        _startTranslate(context, text, translateService.getCurrent(), callback);
    }

    private void _startTranslate(final Context context, final String text, final TranslateServiceModel.TranslateServiceEnum translateService, final OnTranslateManagerCallback callback) {
        final String translateFromLang = OcrNTranslateUtils.getInstance().getTranslateFromLang();
        final String translateToLang = OcrNTranslateUtils.getInstance().getTranslateToLang();

        Tool.logInfo("Translate with " + translateService.name());
        switch (translateService) {
            case google: {
                if (googleTranslateWebView == null) {
                    googleTranslateWebView = new GoogleTranslateWebView(context);
                }
                googleTranslateWebView.startTranslate(text, translateToLang, new GoogleTranslateWebView.OnGoogleTranslateWebViewCallback() {
                    @Override
                    public void onTranslated(final String translatedText) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                FabricUtil.logTranslationInfo(text, translateFromLang, translateToLang, translateService);
                                callback.onTranslateFinished(translatedText);
                            }
                        });
                    }

                    @Override
                    public void onHttpException(int httpStatus, String reason) {
                        _startTranslate(context, text, TranslateServiceModel.TranslateServiceEnum.microsoft, callback);
                    }

                    @Override
                    public void onNoneException() {
                        _startTranslate(context, text, TranslateServiceModel.TranslateServiceEnum.microsoft, callback);
                    }

                    @Override
                    public void onTimeout() {
                        _startTranslate(context, text, TranslateServiceModel.TranslateServiceEnum.microsoft, callback);
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
                                FabricUtil.logTranslationInfo(text, translateFromLang, translateToLang, translateService);
                                callback.onTranslateFinished(translatedText);
                            }
                        });
                    }
                }).execute();
            }
            break;
        }

    }

    public interface OnTranslateManagerCallback {
        void onTranslateFinished(String translatedText);
    }
}
