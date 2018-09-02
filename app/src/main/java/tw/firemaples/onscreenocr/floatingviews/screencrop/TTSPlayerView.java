package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.tts.AndroidTTSManager;
import tw.firemaples.onscreenocr.tts.TTSPlayer;
import tw.firemaples.onscreenocr.tts.TTSRetrieverTask;
import tw.firemaples.onscreenocr.utils.AudioFocusManager;
import tw.firemaples.onscreenocr.utils.HomeWatcher;
import tw.firemaples.onscreenocr.utils.SettingUtil;

/**
 * Created by firemaples on 30/04/2017.
 */

public class TTSPlayerView extends FloatingView {
    private static final Logger logger = LoggerFactory.getLogger(TTSPlayerView.class);

    // http://stackoverflow.com/questions/2159026/regex-how-to-get-words-from-a-string-c
    private static final String PATTERN_WORD = "[^\\W\\d](\\w|[-'\\.]{1,2}(?=\\w))*";

    private AudioFocusManager audioFocusManager;

    private TextView tv_textToSpeech, tv_speed;
    private CheckBox cb_enablePlaySlowly;
    private View bt_play, bt_pause, bt_stop, bt_selectOff, bt_close;
    private SeekBar sb_speed;

    private SettingUtil spUtil;
    private TTSPlayer ttsPlayer;
    private PlayerState playerState = PlayerState.INIT;

    private Handler mainHandler;

    private boolean subTextMode = false;
    private File currentTTSFile;
    private String lang;
    private String ttsContent;

    public TTSPlayerView(Context context) {
        super(context);

        mainHandler = new Handler(context.getMainLooper());
        audioFocusManager = new AudioFocusManager(context);
        audioFocusManager.setCallback(onAudioFocusChangedCallback);
        spUtil = SettingUtil.INSTANCE;
        ttsPlayer = TTSPlayer.getInstance();
        ttsPlayer.setCallback(onTTSPlayCallback);
        setViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_tts_player;
    }

    @Override
    protected int getLayoutGravity() {
        return Gravity.CENTER;
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected boolean layoutFocusable() {
        return true;
    }

    private void setViews(View rootView) {
        tv_textToSpeech = (TextView) rootView.findViewById(R.id.tv_textToSpeech);
        tv_speed = (TextView) rootView.findViewById(R.id.tv_speed);
        cb_enablePlaySlowly = (CheckBox) rootView.findViewById(R.id.cb_enablePlaySlowly);
        bt_play = rootView.findViewById(R.id.bt_play);
        bt_pause = rootView.findViewById(R.id.bt_pause);
        bt_stop = rootView.findViewById(R.id.bt_stop);
        bt_selectOff = rootView.findViewById(R.id.bt_selectOff);
        bt_close = rootView.findViewById(R.id.bt_close);
        sb_speed = (SeekBar) rootView.findViewById(R.id.sb_speed);

        cb_enablePlaySlowly.setOnCheckedChangeListener(onCheckedChangeListener);
        bt_play.setOnClickListener(onClickListener);
        bt_pause.setOnClickListener(onClickListener);
        bt_stop.setOnClickListener(onClickListener);
        bt_selectOff.setOnClickListener(onClickListener);
        bt_close.setOnClickListener(onClickListener);
        sb_speed.setMax(19);

        cb_enablePlaySlowly.setChecked(spUtil.getReadSpeedEnabled());
        sb_speed.setEnabled(spUtil.getReadSpeedEnabled());
        float readSpeed = spUtil.getReadSpeed();
        sb_speed.setProgress((int) (readSpeed * 10) - 1);
        updatePlaySpeed();
        sb_speed.setOnSeekBarChangeListener(onSeekBarChangeListener);

        setupHomeButtonWatcher(onHomePressedListener);

        updateBtnState(PlayerState.INIT);
    }

    private void updateBtnState(final PlayerState state) {
        this.playerState = state;

        logger.info("updateBtnState: " + state.name());

        if (state == PlayerState.PLAYING) {
            audioFocusManager.requestAudioFocus();
        } else if (state == PlayerState.PAUSE || state == PlayerState.STOP) {
            audioFocusManager.abandonAudioFocus();
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (state == PlayerState.INIT) {
                    bt_play.setEnabled(false);
                    bt_pause.setEnabled(false);
                    bt_stop.setEnabled(false);
                    bt_selectOff.setEnabled(false);
                } else {
                    bt_play.setEnabled(state != PlayerState.PLAYING);
                    bt_pause.setEnabled(state == PlayerState.PLAYING);
                    bt_stop.setEnabled(state != PlayerState.STOP);
                    bt_selectOff.setEnabled(subTextMode);
                }
            }
        });
    }

    public void setTTSContent(String lang, String ttsContent) {
        this.lang = lang;
        this.ttsContent = ttsContent;

        tv_textToSpeech.setText(ttsContent);

        makeWordLinks(tv_textToSpeech, ttsContent);

        playTTS(lang, ttsContent);
    }

    private void playTTS(String lang, String text) {
        TTSRetrieverTask ttsRetrieverTask = new TTSRetrieverTask(getContext(), lang, text, onTTSRetrieverCallback);
        ttsRetrieverTask.execute();
        manageTask(ttsRetrieverTask);
    }

    /**
     * https://professorneurus.wordpress.com/2013/10/23/adding-multiple-clicking-regions-to-an-android-textview/
     *
     * @param tv
     * @param text
     */
    private void makeWordLinks(TextView tv, String text) {
        if (tv == null || text == null) {
            return;
        }

        SpannableString ss = new SpannableString(text);
        Pattern pattern = Pattern.compile(PATTERN_WORD);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String group = matcher.group();
            int start = matcher.start();
            int end = matcher.end();
//            logger.d("Matched group:[" + group + "](" + start + "," + end + ")");
            ss.setSpan(new MyClickableSpan(tv, group), start, end, 0);
        }

        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(ss, TextView.BufferType.SPANNABLE);
        tv.getCurrentTextColor();
    }

    private void onWordClicked(String text) {
        logger.info("Selected word: " + text);
        playTTS(lang, text);
        subTextMode = true;
        updateBtnState(playerState);
    }

    private float getPlaySpeedFromSeekBar() {
        return (sb_speed.getProgress() + 1) / 10f;
    }

    private void updatePlaySpeed() {
        if (spUtil.getReadSpeedEnabled()) {
            ttsPlayer.setSpeed(getPlaySpeedFromSeekBar());
        } else {
            ttsPlayer.setSpeed(1);
        }

        float readSpeed = (sb_speed.getProgress() + 1) / 10f;
        tv_speed.setText(String.format(Locale.getDefault(), "x%.1f", readSpeed));

        File silenceFile = AndroidTTSManager.getInstance(getContext()).getSilenceFile();
        if (silenceFile != null) {
            updateBtnState(PlayerState.STOP);
            ttsPlayer.stop();
            ttsPlayer.play(silenceFile);
            ttsPlayer.stop();
        }
    }

    @Override
    public boolean onBackButtonPressed() {
        detachFromWindow();
        return true;
    }

    @Override
    public void detachFromWindow() {
        super.detachFromWindow();
        ttsPlayer.stop();
        audioFocusManager.abandonAudioFocus();
    }

    private void startPlayTTS(final File ttsFile) {
        this.currentTTSFile = ttsFile;

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                updatePlaySpeed();
                ttsPlayer.stop();
                ttsPlayer.play(ttsFile);
                updateBtnState(PlayerState.PLAYING);
            }
        });
    }

    private TTSRetrieverTask.OnTTSRetrieverCallback onTTSRetrieverCallback = new TTSRetrieverTask.OnTTSRetrieverCallback() {
        @Override
        public void onSuccess(File ttsFile) {
            logger.info("Retrieve tts file success, file: " + ttsFile.getAbsolutePath());
            startPlayTTS(ttsFile);
        }

        @Override
        public void onFailed() {
            logger.info("Retrieve tts file failed");
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_close) {
                detachFromWindow();
            } else if (id == R.id.bt_play) {
                if (currentTTSFile != null) {
                    startPlayTTS(currentTTSFile);
                }
            } else if (id == R.id.bt_pause) {
                ttsPlayer.pause();
                updateBtnState(PlayerState.PAUSE);
            } else if (id == R.id.bt_stop) {
                _stop();
            } else if (id == R.id.bt_selectOff) {
                subTextMode = false;
                playTTS(lang, ttsContent);
                makeWordLinks(tv_textToSpeech, ttsContent);
            }
        }
    };

    private void _stop() {
        ttsPlayer.stop();
        updateBtnState(PlayerState.STOP);
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.cb_enablePlaySlowly) {
                spUtil.setReadSpeedEnabled(isChecked);
                sb_speed.setEnabled(isChecked);
                updatePlaySpeed();
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float speed = getPlaySpeedFromSeekBar();
            logger.info("Speed bar changed, progress: " + progress + ", speed: " + speed);
            spUtil.setReadSpeed(speed);
            updatePlaySpeed();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private TTSPlayer.OnTTSPlayCallback onTTSPlayCallback = new TTSPlayer.OnTTSPlayCallback() {
        @Override
        public void onPlayCompletion() {
            updateBtnState(PlayerState.STOP);
        }
    };

    private HomeWatcher.OnHomePressedListener onHomePressedListener = new HomeWatcher.OnHomePressedListener() {
        @Override
        public void onHomePressed() {
            detachFromWindow();
        }

        @Override
        public void onHomeLongPressed() {

        }
    };

    private AudioFocusManager.OnAudioFocusChangedCallback onAudioFocusChangedCallback = new AudioFocusManager.OnAudioFocusChangedCallback() {
        @Override
        public void onAudioFocusLossTransientCanDuck() {
            _stop();
        }

        @Override
        public void onAudioFocusLossTransient() {
            _stop();
        }

        @Override
        public void onAudioFocusLoss() {
            _stop();
        }

        @Override
        public void onAudioFocusGain() {

        }
    };

    private class MyClickableSpan extends ClickableSpan {
        private final TextView tv;
        private final String mText;

        private MyClickableSpan(final TextView tv, final String text) {
            this.tv = tv;
            mText = text;
        }

        @Override
        public void onClick(final View widget) {
            onWordClicked(mText);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(tv.getCurrentTextColor());
        }
    }

    private enum PlayerState {
        INIT, PLAYING, PAUSE, STOP
    }
}
