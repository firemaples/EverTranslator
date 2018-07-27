package tw.firemaples.onscreenocr.translate;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.firemaples.onscreenocr.database.DatabaseManager;
import tw.firemaples.onscreenocr.database.ServiceHolderModel;
import tw.firemaples.onscreenocr.database.ServiceModel;
import tw.firemaples.onscreenocr.utils.FabricUtils;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;

/**
 * Created by firemaples on 01/05/2017.
 */

public class TranslateManager {
    private static final Logger logger = LoggerFactory.getLogger(TranslateManager.class);

    private static TranslateManager _instance;

    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private ServiceHolderModel serviceHolder;

    private GoogleWebTranslator googleWebTranslator;

    private TranslateManager() {
    }

    public static TranslateManager getInstance() {
        if (_instance == null) {
            _instance = new TranslateManager();
        }

        return _instance;
    }

    public void test(Context context) {
        startTranslate(context, "Note Editor", new OnTranslateManagerCallback() {
            @Override
            public void onTranslateFinished(String translatedText) {
                googleWebTranslator = null;
            }
        });
    }

    public void startTranslate(Context context, String text, OnTranslateManagerCallback callback) {
        if (text == null || text.trim().length() == 0 || callback == null) {
            return;
        }
        if (!SharePreferenceUtil.getInstance().isEnableTranslation()) {
            callback.onTranslateFinished(text);
            return;
        }

        OcrNTranslateUtils translateUtils = OcrNTranslateUtils.getInstance();
        if (translateUtils.getTranslateFromLanguage() == translateUtils.getTranslateToLanguage()) {
            callback.onTranslateFinished(text);
            return;
        }

        if (serviceHolder == null) {
            serviceHolder = DatabaseManager.getInstance().getTranslateServiceHolder();
        }

        _startTranslate(context, text, serviceHolder.getUsingService(), callback);
    }

    private void _startTranslate(final Context context, final String text, @Nullable final ServiceModel translateService, final OnTranslateManagerCallback callback) {
        if (translateService == null) {
            callback.onTranslateFinished(null);
            return;
        }

        final String translateFromLang = OcrNTranslateUtils.getInstance().getTranslateFromLang();
        final String translateToLang = OcrNTranslateUtils.getInstance().getTranslateToLang();

        logger.info("Translate with " + translateService.name);
        switch (translateService.name) {
            case ServiceHolderModel.SERVICE_GOOGLE_WEB: {
                if (googleWebTranslator == null) {
                    googleWebTranslator = new GoogleWebTranslator(context);
                }

                final long timeStart = System.currentTimeMillis();
                googleWebTranslator.startTranslate(text, translateToLang, new GoogleWebTranslator.OnGoogleTranslateWebViewCallback() {
                    @Override
                    public void onTranslated(final String translatedText) {
                        logger.info("Translated spent: " + (System.currentTimeMillis() - timeStart) + " ms");
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                FabricUtils.logTranslationInfo(text, translateFromLang, translateToLang, translateService.name);
                                callback.onTranslateFinished(translatedText);
                            }
                        });
                    }

                    @Override
                    public void onHttpException(int httpStatus, String reason) {
                        _startTranslate(context, text, serviceHolder.switchNextService(true), callback);
                    }

                    @Override
                    public void onNoneException() {
                        _startTranslate(context, text, serviceHolder.switchNextService(true), callback);
                    }

                    @Override
                    public void onTimeout() {
                        _startTranslate(context, text, serviceHolder.switchNextService(true), callback);
                    }
                });
            }
            break;
            case ServiceHolderModel.SERVICE_GOOGLE_WEB_API:
                GoogleWebApiTranslator.startTranslate(text, translateToLang, new GoogleWebApiTranslator.OnGoogleTranslateTaskCallback() {

                    @Override
                    public void onTranslated(final String translatedText) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                FabricUtils.logTranslationInfo(text, translateFromLang, translateToLang, translateService.name);
                                callback.onTranslateFinished(translatedText);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        _startTranslate(context, text, serviceHolder.switchNextService(true), callback);
                    }
                });
                break;
            case ServiceHolderModel.SERVICE_MICROSOFT_API: {
                new MicrosoftApiTranslator(context, text, new MicrosoftApiTranslator.OnTranslateAsyncTaskCallback() {
                    @Override
                    public void onTranslateFinished(final String translatedText) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                FabricUtils.logTranslationInfo(text, translateFromLang, translateToLang, translateService.name);
                                callback.onTranslateFinished(translatedText);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        _startTranslate(context, text, serviceHolder.switchNextService(true), callback);
                    }
                }).execute();
            }
            break;
            case ServiceHolderModel.SERVICE_YANDEX_API:
                YandexApiTranslator.startTranslate(text, new YandexApiTranslator.OnYandexTranslateTaskCallback() {
                    @Override
                    public void onTranslated(final String translatedText) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                FabricUtils.logTranslationInfo(text, translateFromLang, translateToLang, translateService.name);
                                callback.onTranslateFinished(translatedText);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        _startTranslate(context, text, serviceHolder.switchNextService(true), callback);
                    }
                });
                break;
        }

    }

    public interface OnTranslateManagerCallback {
        void onTranslateFinished(String translatedText);
    }
}
