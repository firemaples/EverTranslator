package tw.firemaples.onscreenocr.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.memetix.mst.language.Language;

import java.util.Arrays;
import java.util.List;

import tw.firemaples.onscreenocr.R;

/**
 * Created by louis1chen on 31/10/2016.
 */

public class OcrUtils {
    private static OcrUtils _instance;
    private static String DEFAULT_OCR_LANG = "eng";
    private static String DEFAULT_TRANSLATE_LANG = "en";
    /**
     * Key for Text recognition & translation
     */
    public static String KEY_RECOGNITION_LANGUAGE = "preference_list_recognition_language";
    public static String KEY_PAGE_SEGMENTATION_MODE = "preference_list_page_segmentation_mode";
    public static String KEY_TRANSLATE = "preference_switch_translate";
    public static String KEY_TRANSLATION_TO = "preference_list_translation_to";

    private Context context;
    private TessBaseAPI baseAPI;

    private OcrUtils(Context context) {
        this.context = context;
    }

    public static OcrUtils init(Context context) {
        _instance = new OcrUtils(context);
        return _instance;
    }

    public static OcrUtils getInstance() {
        return _instance;
    }

    public TessBaseAPI getBaseAPI() {
        if (baseAPI == null) {
            baseAPI = new TessBaseAPI();
        }
        return baseAPI;
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /* Ocr */
    public List<String> getOcrLangList() {
        return Arrays.asList(context.getResources().getStringArray(R.array.iso6393));
    }

    public List<String> getOcrLangDisplayNameList() {
        return Arrays.asList(context.getResources().getStringArray(R.array.languagenames));
    }

    public String getOcrLang() {
        return getSharedPreferences().getString(KEY_RECOGNITION_LANGUAGE, DEFAULT_OCR_LANG);
    }

    public int getOcrLangIndex() {
        return getOcrLangIndex(getOcrLang());
    }

    public int getOcrLangIndex(String ocrLang) {
        return getOcrLangList().indexOf(ocrLang);
    }

    public String getOcrLangDisplayName() {
        return getOcrLangDisplayNameList().get(getOcrLangIndex());
    }

    public void setOcrLang(String ocrLang) {
        getSharedPreferences().edit().putString(KEY_RECOGNITION_LANGUAGE, ocrLang).apply();
    }

    /* Translate */
    public List<String> getTranslateLangList() {
        return Arrays.asList(
                context.getResources().getStringArray(R.array.translationtargetiso6391_microsoft));
    }

    public List<String> getTranslateLangDisplayNameList() {
        return Arrays.asList(
                context.getResources().getStringArray(R.array.translationtargetlanguagenames_microsoft));
    }

    public String getTranslateFromLang() {
        String iso6393From = getOcrLang();
        return Tool.mapMicrosoftLanguageCode(iso6393From);
    }

    public Language getTranslateFromLanguage() {
        return Language.fromString(getTranslateFromLang());
    }

    public String getTranslateToLang() {
        return getSharedPreferences().getString(KEY_TRANSLATION_TO, DEFAULT_TRANSLATE_LANG);
    }

    public Language getTranslateToLanguage() {
        return Language.fromString(getTranslateToLang());
    }

    public int getTranslateToIndex() {
        return getTranslateLangList().indexOf(getTranslateToLang());
    }

    public void setTranslateTo(String translateToLang) {
        getSharedPreferences().edit().putString(KEY_TRANSLATION_TO, translateToLang).apply();
    }
}
