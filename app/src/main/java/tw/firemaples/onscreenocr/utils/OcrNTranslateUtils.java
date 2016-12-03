package tw.firemaples.onscreenocr.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.memetix.mst.language.Language;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import tw.firemaples.onscreenocr.R;

/**
 * Created by louis1chen on 31/10/2016.
 */

@SuppressWarnings("FieldCanBeLocal")
public class OcrNTranslateUtils {
    private static OcrNTranslateUtils _instance;
    private static String DEFAULT_OCR_LANG = "eng";
    private static String DEFAULT_TRANSLATE_LANG = "en";
    /**
     * Key for Text recognition & translation
     */
    public static String KEY_RECOGNITION_LANGUAGE = "preference_list_recognition_language";
    public static String KEY_PAGE_SEGMENTATION_MODE = "preference_list_page_segmentation_mode";
    public static String KEY_TRANSLATE = "preference_switch_translate";
    public static String KEY_TRANSLATION_TO = "preference_list_translation_to";

    private TessBaseAPI baseAPI;

    private static String[] iso6393Array, microsoftLangArray;

    private OcrNTranslateUtils() {
    }

    public static OcrNTranslateUtils init() {
        _instance = new OcrNTranslateUtils();
        return _instance;
    }

    public static OcrNTranslateUtils getInstance() {
        return _instance;
    }

    public TessBaseAPI getBaseAPI() {
        if (baseAPI == null) {
            baseAPI = new TessBaseAPI();
        }
        return baseAPI;
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(Tool.getContext());
    }

    /* OCR */
    public File getTessDataDir() {
        return new File(Tool.getContext().getFilesDir() + File.separator + "tesseract" + File.separator + "tessdata");
    }

    public List<String> getOcrLangList() {
        return Arrays.asList(Tool.getContext().getResources().getStringArray(R.array.iso6393));
    }

    public List<String> getOcrLangDisplayNameList() {
        return Arrays.asList(Tool.getContext().getResources().getStringArray(R.array.languagenames));
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
                Tool.getContext().getResources().getStringArray(R.array.translationtargetiso6391_microsoft));
    }

    public List<String> getTranslateLangDisplayNameList() {
        return Arrays.asList(
                Tool.getContext().getResources().getStringArray(R.array.translationtargetlanguagenames_microsoft));
    }

    public String getTranslateFromLang() {
        String iso6393From = getOcrLang();
        return mapMicrosoftLanguageCode(iso6393From);
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

    public String getMicrosoftLang(String iso6393Lang) {
        if (Tool.getContext() == null) {
            return null;
        }
        if (iso6393Array == null) {
            iso6393Array = Tool.getContext().getResources().getStringArray(R.array.iso6393);
        }
        int index = -1;
        for (int i = 0, size = iso6393Array.length; i < size; i++) {
            if (iso6393Array[i].equals(iso6393Lang)) {
                index = i;
                break;
            }
        }
        if (microsoftLangArray == null) {
            microsoftLangArray = Tool.getContext().getResources().getStringArray(R.array.translationtargetiso6391_microsoft);
        }
        if (index <= 0 || index >= microsoftLangArray.length) {
            return "en";
        }
        return microsoftLangArray[index];
    }

    /**
     * Map an ISO 639-3 language code to an ISO 639-1 language code.
     * <p>
     * There is one entry here for each language recognized by the OCR engine.
     *
     * @param languageCode ISO 639-3 language code
     * @return ISO 639-1 language code
     */
    public static String mapGoogleLanguageCode(String languageCode) {
        if (languageCode.equals("afr")) { // Afrikaans
            return "af";
        } else if (languageCode.equals("sqi")) { // Albanian
            return "sq";
        } else if (languageCode.equals("ara")) { // Arabic
            return "ar";
        } else if (languageCode.equals("aze")) { // Azeri
            return "az";
        } else if (languageCode.equals("eus")) { // Basque
            return "eu";
        } else if (languageCode.equals("bel")) { // Belarusian
            return "be";
        } else if (languageCode.equals("ben")) { // Bengali
            return "bn";
        } else if (languageCode.equals("bul")) { // Bulgarian
            return "bg";
        } else if (languageCode.equals("cat")) { // Catalan
            return "ca";
        } else if (languageCode.equals("chi_sim")) { // Chinese (Simplified)
            return "zh-CN";
        } else if (languageCode.equals("chi_tra")) { // Chinese (Traditional)
            return "zh-TW";
        } else if (languageCode.equals("hrv")) { // Croatian
            return "hr";
        } else if (languageCode.equals("ces")) { // Czech
            return "cs";
        } else if (languageCode.equals("dan")) { // Danish
            return "da";
        } else if (languageCode.equals("nld")) { // Dutch
            return "nl";
        } else if (languageCode.equals("eng")) { // English
            return "en";
        } else if (languageCode.equals("est")) { // Estonian
            return "et";
        } else if (languageCode.equals("fin")) { // Finnish
            return "fi";
        } else if (languageCode.equals("fra")) { // French
            return "fr";
        } else if (languageCode.equals("glg")) { // Galician
            return "gl";
        } else if (languageCode.equals("deu")) { // German
            return "de";
        } else if (languageCode.equals("ell")) { // Greek
            return "el";
        } else if (languageCode.equals("heb")) { // Hebrew
            return "he";
        } else if (languageCode.equals("hin")) { // Hindi
            return "hi";
        } else if (languageCode.equals("hun")) { // Hungarian
            return "hu";
        } else if (languageCode.equals("isl")) { // Icelandic
            return "is";
        } else if (languageCode.equals("ind")) { // Indonesian
            return "id";
        } else if (languageCode.equals("ita")) { // Italian
            return "it";
        } else if (languageCode.equals("jpn")) { // Japanese
            return "ja";
        } else if (languageCode.equals("kan")) { // Kannada
            return "kn";
        } else if (languageCode.equals("kor")) { // Korean
            return "ko";
        } else if (languageCode.equals("lav")) { // Latvian
            return "lv";
        } else if (languageCode.equals("lit")) { // Lithuanian
            return "lt";
        } else if (languageCode.equals("mkd")) { // Macedonian
            return "mk";
        } else if (languageCode.equals("msa")) { // Malay
            return "ms";
        } else if (languageCode.equals("mal")) { // Malayalam
            return "ml";
        } else if (languageCode.equals("mlt")) { // Maltese
            return "mt";
        } else if (languageCode.equals("nor")) { // Norwegian
            return "no";
        } else if (languageCode.equals("pol")) { // Polish
            return "pl";
        } else if (languageCode.equals("por")) { // Portuguese
            return "pt";
        } else if (languageCode.equals("ron")) { // Romanian
            return "ro";
        } else if (languageCode.equals("rus")) { // Russian
            return "ru";
        } else if (languageCode.equals("srp")) { // Serbian (Latin) // TODO is google expecting Cyrillic?
            return "sr";
        } else if (languageCode.equals("slk")) { // Slovak
            return "sk";
        } else if (languageCode.equals("slv")) { // Slovenian
            return "sl";
        } else if (languageCode.equals("spa")) { // Spanish
            return "es";
        } else if (languageCode.equals("swa")) { // Swahili
            return "sw";
        } else if (languageCode.equals("swe")) { // Swedish
            return "sv";
        } else if (languageCode.equals("tgl")) { // Tagalog
            return "tl";
        } else if (languageCode.equals("tam")) { // Tamil
            return "ta";
        } else if (languageCode.equals("tel")) { // Telugu
            return "te";
        } else if (languageCode.equals("tha")) { // Thai
            return "th";
        } else if (languageCode.equals("tur")) { // Turkish
            return "tr";
        } else if (languageCode.equals("ukr")) { // Ukrainian
            return "uk";
        } else if (languageCode.equals("vie")) { // Vietnamese
            return "vi";
        } else {
            return "";
        }
    }

    /**
     * Map an ISO 639-3 language code to an ISO 639-1 language code.
     * <p>
     * There is one entry here for each language recognized by the OCR engine.
     *
     * @param languageCode ISO 639-3 language code
     * @return ISO 639-1 language code
     */
    public static String mapMicrosoftLanguageCode(String languageCode) {
        if (languageCode.equals("afr")) { // Afrikaans
            return "af";
        } else if (languageCode.equals("sqi")) { // Albanian
            return "sq";
        } else if (languageCode.equals("ara")) { // Arabic
            return "ar";
        } else if (languageCode.equals("aze")) { // Azeri
            return "az";
        } else if (languageCode.equals("eus")) { // Basque
            return "eu";
        } else if (languageCode.equals("bel")) { // Belarusian
            return "be";
        } else if (languageCode.equals("ben")) { // Bengali
            return "bn";
        } else if (languageCode.equals("bul")) { // Bulgarian
            return "bg";
        } else if (languageCode.equals("cat")) { // Catalan
            return "ca";
        } else if (languageCode.equals("chi_sim")) { // Chinese (Simplified)
            return "zh-CHS";
        } else if (languageCode.equals("chi_tra")) { // Chinese (Traditional)
            return "zh-CHT";
        } else if (languageCode.equals("hrv")) { // Croatian
            return "hr";
        } else if (languageCode.equals("ces")) { // Czech
            return "cs";
        } else if (languageCode.equals("dan")) { // Danish
            return "da";
        } else if (languageCode.equals("nld")) { // Dutch
            return "nl";
        } else if (languageCode.equals("eng")) { // English
            return "en";
        } else if (languageCode.equals("est")) { // Estonian
            return "et";
        } else if (languageCode.equals("fin")) { // Finnish
            return "fi";
        } else if (languageCode.equals("fra")) { // French
            return "fr";
        } else if (languageCode.equals("glg")) { // Galician
            return "gl";
        } else if (languageCode.equals("deu")) { // German
            return "de";
        } else if (languageCode.equals("ell")) { // Greek
            return "el";
        } else if (languageCode.equals("heb")) { // Hebrew
            return "he";
        } else if (languageCode.equals("hin")) { // Hindi
            return "hi";
        } else if (languageCode.equals("hun")) { // Hungarian
            return "hu";
        } else if (languageCode.equals("isl")) { // Icelandic
            return "is";
        } else if (languageCode.equals("ind")) { // Indonesian
            return "id";
        } else if (languageCode.equals("ita")) { // Italian
            return "it";
        } else if (languageCode.equals("jpn")) { // Japanese
            return "ja";
        } else if (languageCode.equals("kan")) { // Kannada
            return "kn";
        } else if (languageCode.equals("kor")) { // Korean
            return "ko";
        } else if (languageCode.equals("lav")) { // Latvian
            return "lv";
        } else if (languageCode.equals("lit")) { // Lithuanian
            return "lt";
        } else if (languageCode.equals("mkd")) { // Macedonian
            return "mk";
        } else if (languageCode.equals("msa")) { // Malay
            return "ms";
        } else if (languageCode.equals("mal")) { // Malayalam
            return "ml";
        } else if (languageCode.equals("mlt")) { // Maltese
            return "mt";
        } else if (languageCode.equals("nor")) { // Norwegian
            return "no";
        } else if (languageCode.equals("pol")) { // Polish
            return "pl";
        } else if (languageCode.equals("por")) { // Portuguese
            return "pt";
        } else if (languageCode.equals("ron")) { // Romanian
            return "ro";
        } else if (languageCode.equals("rus")) { // Russian
            return "ru";
        } else if (languageCode.equals("srp")) { // Serbian (Latin) // TODO is google expecting Cyrillic?
            return "sr";
        } else if (languageCode.equals("slk")) { // Slovak
            return "sk";
        } else if (languageCode.equals("slv")) { // Slovenian
            return "sl";
        } else if (languageCode.equals("spa")) { // Spanish
            return "es";
        } else if (languageCode.equals("swa")) { // Swahili
            return "sw";
        } else if (languageCode.equals("swe")) { // Swedish
            return "sv";
        } else if (languageCode.equals("tgl")) { // Tagalog
            return "tl";
        } else if (languageCode.equals("tam")) { // Tamil
            return "ta";
        } else if (languageCode.equals("tel")) { // Telugu
            return "te";
        } else if (languageCode.equals("tha")) { // Thai
            return "th";
        } else if (languageCode.equals("tur")) { // Turkish
            return "tr";
        } else if (languageCode.equals("ukr")) { // Ukrainian
            return "uk";
        } else if (languageCode.equals("vie")) { // Vietnamese
            return "vi";
        } else {
            return "";
        }
    }
}
