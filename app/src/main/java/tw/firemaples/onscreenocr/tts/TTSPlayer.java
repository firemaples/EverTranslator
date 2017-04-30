package tw.firemaples.onscreenocr.tts;

import android.media.MediaPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class TTSPlayer {
    private static TTSPlayer _instance;

    private MediaPlayer mediaPlayer;
    private OnTTSPlayCallback callback;

    private boolean pausing = false;
    private int pausingPosition = 0;

    private TTSPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(onCompletionListener);
    }

    public static TTSPlayer getInstance() {
        if (_instance == null) {
            _instance = new TTSPlayer();
        }

        return _instance;
    }

    public void setCallback(OnTTSPlayCallback callback) {
        this.callback = callback;
    }

    public void play(File ttsFile) {
        try {
            if (!pausing) {
                FileInputStream fileInputStream = new FileInputStream(ttsFile);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(fileInputStream.getFD());
                mediaPlayer.prepare();
            } else {
                mediaPlayer.seekTo(pausingPosition);
            }
            mediaPlayer.start();
            pausing = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying() && !pausing) {
            pausing = true;
            mediaPlayer.pause();
            pausingPosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void stop() {
        pausing = false;
        mediaPlayer.stop();
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (callback != null) {
                callback.onPlayCompletion();
            }
        }
    };

    public interface OnTTSPlayCallback {
        void onPlayCompletion();

        void onPlayStart();

    }
}
