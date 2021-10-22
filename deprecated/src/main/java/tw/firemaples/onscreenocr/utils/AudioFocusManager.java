package tw.firemaples.onscreenocr.utils;

import android.content.Context;
import android.media.AudioManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by firemaples on 31/05/2017.
 */

/**
 * https://stackoverflow.com/questions/15390126/how-to-stop-other-apps-playing-music-from-my-current-activity
 * https://developer.android.com/guide/topics/media-apps/volume-and-earphones.html
 * <p>
 * https://stackoverflow.com/questions/21633495/how-to-pause-different-music-players-in-android/21633772#21633772
 */
public class AudioFocusManager {
    private static final Logger logger = LoggerFactory.getLogger(AudioFocusManager.class);

    protected Context context;

    protected AudioManager audioManager;

    private OnAudioFocusChangedCallback callback;

    private boolean isRequesting = false;

//    private boolean isOtherMusicAppPlaying = false;

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    logger.debug("onAudioFocusChange(), AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    if (callback != null) {
                        callback.onAudioFocusLossTransientCanDuck();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    logger.debug("onAudioFocusChange(), AUDIOFOCUS_LOSS_TRANSIENT");
                    if (callback != null) {
                        callback.onAudioFocusLossTransient();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    logger.debug("onAudioFocusChange(), AUDIOFOCUS_LOSS");
                    if (callback != null) {
                        callback.onAudioFocusLoss();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    logger.debug("onAudioFocusChange(), AUDIOFOCUS_GAIN");
                    if (callback != null) {
                        callback.onAudioFocusGain();
                    }
                    break;
            }
        }
    };

    public AudioFocusManager(Context context) {
        this.context = context;
        audioManager = ((AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
    }

    public void setCallback(OnAudioFocusChangedCallback callback) {
        this.callback = callback;
    }

    public boolean requestAudioFocus() {
        logger.debug("requestAudioFocus()");
        if (isRequesting) {
            logger.debug("Current is requesting, ignore this action");
            return true;
        }

//        isOtherMusicAppPlaying = audioManager.isMusicActive();

        int result = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        isRequesting = true;

        boolean resultBoolean = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        logger.debug("requestAudioFocus(), result: " + resultBoolean);

//        if (isOtherMusicAppPlaying) {
//            sendMediaPauseButton();
//        }

        return resultBoolean;
    }

    public void abandonAudioFocus() {
        logger.debug("abandonAudioFocus()");
        if (!isRequesting) {
            logger.debug("Current is not requesting, ignore this action");
            return;
        }

        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        isRequesting = false;

//        onAudioFocusChangeListener = null;

//        if (isOtherMusicAppPlaying) {
//            isOtherMusicAppPlaying = false;
//            sendMediaPlayButton();
//        }
    }

    public interface OnAudioFocusChangedCallback {
        /**
         * Lower the volume and keep playing
         * <p>
         * See {@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK}
         */
        void onAudioFocusLossTransientCanDuck();

        /**
         * Pause playback
         * <p>
         * See {@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT}
         */
        void onAudioFocusLossTransient();

        /**
         * Stop playback and never gain the focus again
         * <p>
         * See {@link AudioManager#AUDIOFOCUS_LOSS}
         */
        void onAudioFocusLoss();

        /**
         * Raise the volume to normal or restart playback if necessary
         * <p>
         * See {@link AudioManager#AUDIOFOCUS_GAIN}
         */
        void onAudioFocusGain();
    }
}
