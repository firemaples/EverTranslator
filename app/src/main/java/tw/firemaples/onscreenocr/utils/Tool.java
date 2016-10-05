package tw.firemaples.onscreenocr.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import tw.firemaples.onscreenocr.R;

/**
 * Created by Louis on 2016/3/1.
 */
public class Tool {
    private static Context context;

    private static String LOG_TAG = "OnScreenOcr";

    private static String[] iso6393Array, microsoftLangArray;

    public static void init(Context context) {
        Tool.context = context;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void logError(String msg) {
        Log.e(LOG_TAG, msg);
    }

    public static void logInfo(String msg) {
        Log.i(LOG_TAG, msg);
    }

    public static void showMsg(String msg) {
        if (context == null) {
            return;
        }
        SuperToast.create(context, msg, SuperToast.Duration.LONG,
                Style.getStyle(Style.GREEN, SuperToast.Animations.FADE)).show();
    }

    public static void showErrorMsg(String msg) {
        if (context == null) {
            return;
        }
        SuperToast.create(context, msg, SuperToast.Duration.LONG,
                Style.getStyle(Style.RED, SuperToast.Animations.FADE)).show();
    }

    public static String getMicrosoftLang(String iso6393Lang) {
        if (context == null) {
            return null;
        }
        if (iso6393Array == null) {
            iso6393Array = context.getResources().getStringArray(R.array.iso6393);
        }
        int index = -1;
        for (int i = 0, size = iso6393Array.length; i < size; i++) {
            if (iso6393Array[i].equals(iso6393Lang)) {
                index = i;
                break;
            }
        }
        if (microsoftLangArray == null) {
            microsoftLangArray = context.getResources().getStringArray(R.array.translationtargetiso6391_microsoft);
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
