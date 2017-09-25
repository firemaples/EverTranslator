package tw.firemaples.onscreenocr.translate;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import io.github.firemaples.language.Language;
import tw.firemaples.onscreenocr.database.ServiceHolderModel;
import tw.firemaples.onscreenocr.utils.OcrNTranslateUtils;
import tw.firemaples.onscreenocr.utils.Tool;
import tw.firemaples.onscreenocr.utils.UrlFormatter;

/**
 * Created by firemaples on 13/09/2017.
 */

public class YandexApiTranslator {
    private static final int CODE_RESULT_OK = 200;

    public static void startTranslate(String textToTranslate, final OnYandexTranslateTaskCallback callback) {

        String targetLanguage = OcrNTranslateUtils.getInstance().getTranslateToLang();
        final Language targetLanguageObject = OcrNTranslateUtils.getInstance().getTranslateToLanguage();

        String lang = targetLanguage.split("-")[0];

        String url = UrlFormatter.getFormattedUrl(ServiceHolderModel.SERVICE_YANDEX_API, textToTranslate, lang);

        Tool.logInfo("YandexApiTranslator start loading url:" + url);

        AndroidNetworking.get(url).setPriority(Priority.HIGH)
                .build()
                .getAsObject(TranslateResult.class, new ParsedRequestListener() {
                    @Override
                    public void onResponse(Object response) {
                        if (response != null) {
                            TranslateResult result = (TranslateResult) response;
                            Tool.logInfo("YandexApiTranslator: result code:" + result.code);
                            if (result.code == CODE_RESULT_OK
                                    && result.text != null && result.text.length > 0
                                    && result.text[0].trim().length() > 0) {
                                String text = result.text[0];
                                if (targetLanguageObject == Language.CHINESE_TRADITIONAL) {
                                    text = ChineseTSConverter.StoT(text);
                                }
                                Tool.logInfo("YandexApiTranslator: result found:" + text);
                                callback.onTranslated(text);
                                return;
                            } else {
                                Exception exception = new Exception("Yandex api response code is not " + CODE_RESULT_OK);
                                exception.printStackTrace();
                                callback.onError(exception);
                            }
                        } else {
                            Exception exception = new Exception("Yandex api response null object");
                            exception.printStackTrace();
                            callback.onError(exception);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Tool.logError("YandexApiTranslator: error: " + anError.getMessage() + "(" + anError.getErrorCode() + ")");
                        Tool.logError(anError.getErrorBody());
                        callback.onError(anError);
                    }
                });
    }

    public interface OnYandexTranslateTaskCallback {
        void onTranslated(String translatedText);

        void onError(Throwable throwable);
    }

    public static class TranslateResult {
        public int code;
        public String lang;
        public String[] text;
    }
}
