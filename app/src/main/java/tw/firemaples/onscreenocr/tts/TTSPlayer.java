package tw.firemaples.onscreenocr.tts;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import tw.firemaples.onscreenocr.utils.Callback;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 30/04/2017.
 */

public class TTSPlayer {
    private static TTSPlayer _instance;

    private Context context;

    private static final long TIME_MAX_CHANGE_SPEED_WAIT = 1500;

    private OnTTSPlayCallback callback;

    private SimpleExoPlayer player;
    private DataSource.Factory dataSourceFactory;
    private ExtractorsFactory extractorsFactory;

    private File currentTTSFile;

    private PlaySpeedSettingTask playSpeedSettingTask;

    private TTSPlayer() {
        setupPlayer(Tool.getContext());
    }

    public static TTSPlayer getInstance() {
        if (_instance == null) {
            _instance = new TTSPlayer();
        }

        return _instance;
    }

    private void setupPlayer(Context context) {
        this.context = context;
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(null);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

// 2. Create the player
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

// Produces DataSource instances through which media data is loaded.
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "yourApplicationName"), null);
// Produces Extractor instances for parsing the media data.
        extractorsFactory = new DefaultExtractorsFactory();

        player.addListener(eventListener);

        player.setPlayWhenReady(true);
    }

    public void setCallback(OnTTSPlayCallback callback) {
        this.callback = callback;
    }

    public void play(File ttsFile) {
        if (currentTTSFile == null || !ttsFile.getAbsolutePath().equals(currentTTSFile.getAbsolutePath())) {
            currentTTSFile = ttsFile;
            player.setPlayWhenReady(true);
            // This is the MediaSource representing the media to be played.
            MediaSource videoSource = new ExtractorMediaSource(Uri.fromFile(ttsFile),
                    dataSourceFactory, extractorsFactory, null, null);
            // Prepare the player with the source.
            player.prepare(videoSource);
        } else {
            player.setPlayWhenReady(true);
        }
    }

    public void pause() {
        player.setPlayWhenReady(false);
    }

    public void stop() {
        player.setPlayWhenReady(false);
        player.seekTo(0);
    }

    public void setSpeed(final float speed) {

        player.setPlaybackParameters(new PlaybackParameters(speed, 1f));
//        new PlaySpeedSettingTask(speed, onSpeedChangedCallback).execute();
//        if (speed == currentSpeed) {
//            if (onSpeedChangedCallback != null) {
//                onSpeedChangedCallback.onCallback(null);
//            }
//        } else {
//            this.currentSpeed = speed;
//            this.onSpeedChangedCallback = onSpeedChangedCallback;
//            player.setPlaybackParameters(new PlaybackParameters(speed, 1f));
//        }
    }

    public float getPlaySpeed() {
        return player.getPlaybackParameters().speed;
    }

    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == ExoPlayer.STATE_ENDED) {
                player.setPlayWhenReady(false);
                player.seekTo(0);
                if (callback != null) {
                    callback.onPlayCompletion();
                }
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity() {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }
    };

    private class PlaySpeedSettingTask extends AsyncTask<Void, Void, Boolean> {

        private final float speed;
        private final Callback<Boolean> callback;
        private long stopTime;

        private PlaySpeedSettingTask(float speed, @Nullable Callback<Boolean> callback) {
            this.speed = speed;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (playSpeedSettingTask != null && !playSpeedSettingTask.isCancelled()) {
                playSpeedSettingTask.cancel(true);
            }
            TTSPlayer.this.playSpeedSettingTask = this;

            player.setPlaybackParameters(new PlaybackParameters(speed, 1f));
            stopTime = System.currentTimeMillis() + TIME_MAX_CHANGE_SPEED_WAIT;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (callback == null) {
                return null;
            }
            if (player.getPlaybackParameters().speed == speed) {
                return false;
            }
            while (player.getPlaybackParameters().speed != speed) {
                if (System.currentTimeMillis() > stopTime) {
                    Tool.logInfo("Waiting play speed change timeout");
                    break;
                }
                Tool.logInfo("wait for set play speed effect");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            AndroidTTSManager ttsManager = AndroidTTSManager.getInstance(context);
            if (ttsManager.getSilenceFile() != null) {
                stop();
                play(ttsManager.getSilenceFile());
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (player.getPlaybackParameters().speed == speed) {
                Tool.logInfo("Set play speed effected!");
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean hasChanged) {
            super.onPostExecute(hasChanged);
            if (callback != null && !isCancelled()) {
                callback.onCallback(hasChanged);
            }
        }
    }

    public interface OnTTSPlayCallback {
        void onPlayCompletion();
    }
}
