package tw.firemaples.onscreenocr.tts;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.io.File;
import java.util.Locale;

import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class AndroidTTSManager implements TTSManager {
    private static AndroidTTSManager _instance;

    private static final String PATH_TTS_FILE = "tts";
    private static final String REQUEST_ID = "tts";

    private final Context context;

    private TextToSpeech tts;

    private Boolean ttsReady;

    private String lastLang;
    private String lastTTSContent;
    private File ttsFolder;

    private AndroidTTSManager(Context context) {
        this.context = context;

        ttsFolder = new File(context.getCacheDir(), PATH_TTS_FILE);
        if (!ttsFolder.exists()) {
            ttsFolder.mkdirs();
        }
    }

    public static AndroidTTSManager getInstance(Context context) {
        if (_instance == null) {
            _instance = new AndroidTTSManager(context);
        }

        return _instance;
    }

    public void init() {
        if (tts == null) {
            ttsReady = null;
            tts = new TextToSpeech(context, onInitListener);
            tts.setOnUtteranceProgressListener(utteranceProgressListener);
        }
    }

    private File getTTSFile(String lang, String ttsContent) {
        String fileName = lang + "_" + ttsContent.replaceAll(" ", "_");
        File ttsFile = new File(ttsFolder.getAbsolutePath(), fileName);
        return ttsFile;
    }

    @Override
    public synchronized File retrieveTTSFile(String lang, String ttsContent) throws LanguageNotSupportException {
        this.lastLang = lang;
        this.lastTTSContent = ttsContent;

        if (ttsReady) {
            int setLangResult = tts.setLanguage(new Locale(lang));
            if (setLangResult == TextToSpeech.LANG_MISSING_DATA ||
                    setLangResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Lanuage data is missing or the language is not supported.
                Tool.logError("retrieveTTSFile failed, Language is not available.");
                throw new LanguageNotSupportException(String.format(Locale.getDefault(), "Language [%s] is not available.", lang));
            } else {
                File ttsFile = getTTSFile(lang, ttsContent);
                int requestFileResult = tts.synthesizeToFile(ttsContent, null, ttsFile, REQUEST_ID);
                if (requestFileResult == TextToSpeech.SUCCESS) {
                    return ttsFile;
                } else {
                    Tool.logError("retrieveTTSFile failed, failed to synthesizeToFile.");
                    return null;
                }
            }
        }

        return null;
    }

    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            Tool.logInfo("onInit()");
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true;
            } else {
                ttsReady = false;
                // Initialization failed.
                Tool.logError("Could not initialize TextToSpeech.");
                // May be its not installed so we prompt it to be installed
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                context.startActivity(installIntent);
            }
        }
    };

    private UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            Tool.logInfo("utteranceProgressListener#onStart()");
        }

        @Override
        public void onDone(String utteranceId) {
            Tool.logInfo("utteranceProgressListener#onStart()");
        }

        @Override
        public void onError(String utteranceId) {
            Tool.logError("utteranceProgressListener#onStart()");
        }
    };

    public class LanguageNotSupportException extends Exception {
        LanguageNotSupportException(String msg) {
            super(msg);
        }
    }
}
