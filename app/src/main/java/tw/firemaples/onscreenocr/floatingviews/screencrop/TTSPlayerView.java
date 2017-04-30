package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.tts.TTSPlayer;
import tw.firemaples.onscreenocr.tts.TTSRetrieverTask;
import tw.firemaples.onscreenocr.utils.HomeWatcher;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class TTSPlayerView extends FloatingView {
    private TextView tv_textToSpeech;
    private View bt_play, bt_pause, bt_stop, bt_time, bt_selectOff, bt_close;

    private TTSPlayer ttsPlayer;
    private PlayerState playerState = PlayerState.INIT;

    private Handler mainHandler;

    private File currentTTSFile;
    private String ttsContent;

    public TTSPlayerView(Context context) {
        super(context);

        mainHandler = new Handler(context.getMainLooper());
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

    private void setViews(View rootView) {
        tv_textToSpeech = (TextView) rootView.findViewById(R.id.tv_textToSpeech);
        bt_play = rootView.findViewById(R.id.bt_play);
        bt_pause = rootView.findViewById(R.id.bt_pause);
        bt_stop = rootView.findViewById(R.id.bt_stop);
        bt_time = rootView.findViewById(R.id.bt_time);
        bt_selectOff = rootView.findViewById(R.id.bt_selectOff);
        bt_close = rootView.findViewById(R.id.bt_close);

        bt_play.setOnClickListener(onClickListener);
        bt_pause.setOnClickListener(onClickListener);
        bt_stop.setOnClickListener(onClickListener);
        bt_time.setOnClickListener(onClickListener);
        bt_selectOff.setOnClickListener(onClickListener);
        bt_close.setOnClickListener(onClickListener);

        setupHomeButtonWatcher(onHomePressedListener);

        updateBtnState(PlayerState.INIT);
    }

    private void updateBtnState(final PlayerState state) {
        this.playerState = state;

        Tool.logInfo("updateBtnState: " + state.name());

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (state == PlayerState.INIT) {
                    bt_play.setEnabled(false);
                    bt_pause.setEnabled(false);
                    bt_stop.setEnabled(false);
                } else {
                    bt_play.setEnabled(state != PlayerState.PLAYING);
                    bt_pause.setEnabled(state == PlayerState.PLAYING);
                    bt_stop.setEnabled(state != PlayerState.STOP);
                }
            }
        });
    }

    public void setTTSContent(String lang, String ttsContent) {
        this.ttsContent = ttsContent;

        tv_textToSpeech.setText(ttsContent);

        TTSRetrieverTask ttsRetrieverTask = new TTSRetrieverTask(getContext(), lang, ttsContent, onTTSRetrieverCallback);
        ttsRetrieverTask.execute();
        manageTask(ttsRetrieverTask);
    }

    @Override
    public boolean onBackButtonPressed() {
        detachFromWindow();
        return true;
    }

    private void startPlayTTS(File ttsFile) {
        this.currentTTSFile = ttsFile;
        ttsPlayer.play(ttsFile);
        updateBtnState(PlayerState.PLAYING);
    }

    private TTSRetrieverTask.OnTTSRetrieverCallback onTTSRetrieverCallback = new TTSRetrieverTask.OnTTSRetrieverCallback() {
        @Override
        public void onSuccess(File ttsFile) {
            Tool.logInfo("Retrieve tts file success, file: " + ttsFile.getAbsolutePath());
            startPlayTTS(ttsFile);
        }

        @Override
        public void onFailed() {
            Tool.logInfo("Retrieve tts file failed");
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_close) {
                detachFromWindow();
            } else if (id == R.id.bt_play) {
                startPlayTTS(currentTTSFile);
            } else if (id == R.id.bt_pause) {
                ttsPlayer.pause();
                updateBtnState(PlayerState.PAUSE);
            } else if (id == R.id.bt_stop) {
                ttsPlayer.stop();
                updateBtnState(PlayerState.STOP);
            }
        }
    };

    private TTSPlayer.OnTTSPlayCallback onTTSPlayCallback = new TTSPlayer.OnTTSPlayCallback() {
        @Override
        public void onPlayCompletion() {
            updateBtnState(PlayerState.STOP);
        }

        @Override
        public void onPlayStart() {
            updateBtnState(PlayerState.PLAYING);
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

    private enum PlayerState {
        INIT, PLAYING, PAUSE, STOP
    }
}
