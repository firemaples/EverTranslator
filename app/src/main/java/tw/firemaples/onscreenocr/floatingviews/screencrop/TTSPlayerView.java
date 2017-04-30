package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.tts.TTSRetrieverTask;
import tw.firemaples.onscreenocr.utils.HomeWatcher;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class TTSPlayerView extends FloatingView {
    private TextView tv_textToSpeech;
    private View bt_play, bt_pause, bt_stop, bt_time, bt_selectOff, bt_close;

    private String ttsContent;

    private TTSRetrieverTask ttsRetrieverTask;

    public TTSPlayerView(Context context) {
        super(context);

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

    private TTSRetrieverTask.OnTTSRetrieverCallback onTTSRetrieverCallback = new TTSRetrieverTask.OnTTSRetrieverCallback() {
        @Override
        public void onSuccess(File ttsFile) {
            Tool.logInfo("Retrieve tts file success, file: " + ttsFile.getAbsolutePath());
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
            }
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
}
