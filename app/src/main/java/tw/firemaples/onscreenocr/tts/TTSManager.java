package tw.firemaples.onscreenocr.tts;

import java.io.File;

/**
 * Created by louis1chen on 30/04/2017.
 */

public interface TTSManager {
    public File retrieveTTSFile(String lang, String ttsContent) throws AndroidTTSManager.LanguageNotSupportException;
}
