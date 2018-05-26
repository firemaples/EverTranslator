package tw.firemaples.onscreenocr.utils;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Locale;

import tw.firemaples.onscreenocr.translate.GoogleWebApiTranslator;

import static tw.firemaples.onscreenocr.utils.Tool.getIPAddress;

/**
 * Created by firemaples on 08/06/2017.
 */

public class FabricUtil {
    private static final Logger logger = LoggerFactory.getLogger(FabricUtil.class);

    public static void logClientInfo() {
        String str = "Language:" + Locale.getDefault().getLanguage() + "\r\n" +
                "Display language:" + Locale.getDefault().getDisplayLanguage() + "\r\n" +
                "Country code:" + Tool.getContext().getResources().getConfiguration().locale.getCountry() + "\r\n" +
                "Country:" + Tool.getContext().getResources().getConfiguration().locale.getDisplayCountry() + "\r\n" +
                "Ip:" + getIPAddress(true) + "\r\n";

        Crashlytics.log(str);
    }

    public static void logAppLaunched() {
        Answers.getInstance().logCustom(new CustomEvent("App launched"));
    }

    public static void logBtnSelectAreaClicked() {
        Answers.getInstance().logCustom(new CustomEvent("Btn select area clicked"));
    }

    public static void logDoBtnSelectAreaAction() {
        Answers.getInstance().logCustom(new CustomEvent("Do btn select area action"));
    }

    public static void logBtnTranslationClicked() {
        Answers.getInstance().logCustom(new CustomEvent("Btn translation clicked"));
    }

    public static void logDoBtnTranslationAction() {
        Answers.getInstance().logCustom(new CustomEvent("Do btn translation action"));
    }

    public static void logStartScreenshotOperation() {
        Answers.getInstance().logCustom(new CustomEvent("Start screenshot operation"));
    }

    public static void logFinishScreenshotOperation(long spentTime) {
        Answers.getInstance().logCustom(new CustomEvent("Finished screenshot operation")
                .putCustomAttribute("Spent ms", spentTime));
    }

    public static void logStartOCROperation() {
        Answers.getInstance().logCustom(new CustomEvent("Start OCR operation"));
    }

    public static void logStartTranslateOperation() {
        Answers.getInstance().logCustom(new CustomEvent("Start Translate operation"));
    }

    public static void logFinishTranslateOperation() {
        Answers.getInstance().logCustom(new CustomEvent("Finish translate operation"));
    }

    public static void logBtnClearClicked() {
        Answers.getInstance().logCustom(new CustomEvent("Btn clear clicked"));
    }

    public static void logBtnOpenOnOtherBrowserClicked() {
        Answers.getInstance().logCustom(new CustomEvent("Btn open on other browser clicked"));
    }

    public static void logBtnCopyToClipboardClicked(String label) {
        Answers.getInstance().logCustom(new CustomEvent("Btn copy to clipboard clicked").putCustomAttribute("Type", label));
    }

    public static void logBtnOpenInWebViewClicked(String type) {
        Answers.getInstance().logCustom(new CustomEvent("Btn open in webview clicked").putCustomAttribute("Type", type));
    }

    public static void logBtnPlayTTSClicked(String type) {
        Answers.getInstance().logCustom(new CustomEvent("Btn play TTS clicked").putCustomAttribute("Type", type));
    }

    public static void logTranslationInfo(String text, String translateFromLang, String translateToLang, String serviceName) {
        Answers.getInstance().logCustom(
                new CustomEvent("Translate Text")
                        .putCustomAttribute("Text length", text.length())
                        .putCustomAttribute("Translate from", translateFromLang)
                        .putCustomAttribute("Translate to", translateToLang)
                        .putCustomAttribute("From > to", translateFromLang + " > " + translateToLang)
                        .putCustomAttribute("System language", Locale.getDefault().getLanguage())
                        .putCustomAttribute("Translate service", serviceName)
        );
    }

    public static void postOnGoogleTranslateFailedException(int httpStatus, String reason) {
        String msg = String.format(Locale.getDefault(),
                "Google translation can't not be reached, http status code: %d, reason: %s"
                , httpStatus, reason
        );

        logger.error(msg);
        logClientInfo();
        Crashlytics.logException(
                new ConnectException(msg));
        logClientInfo();
    }

    public static void postOnGoogleTranslateFailedWithNoneContentException() {
        logClientInfo();
        Crashlytics.logException(new Exception("Google translate failed with none content exception"));
        logClientInfo();
    }

    public static void postOnGoogleTranslateTimeout(long timeout) {
        logClientInfo();
        Crashlytics.logException(new Exception("Google translate timeout, timeout setting:" + timeout));
        logClientInfo();
    }

    public static void postException(Throwable t) {
        logClientInfo();
        Crashlytics.logException(t);
        logClientInfo();
    }

    public static void logGoogleTranslateResultNotFoundException(GoogleWebApiTranslator.GoogleTranslateResultNotFoundException e, String html, String resultParser) {
        logClientInfo();
        Crashlytics.setString("ResultParser", resultParser);
        Crashlytics.log("html=" + html);
        Crashlytics.logException(e);
        logClientInfo();
    }
}
