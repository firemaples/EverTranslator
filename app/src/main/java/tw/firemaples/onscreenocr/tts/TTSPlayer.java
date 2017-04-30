package tw.firemaples.onscreenocr.tts;

import android.content.Context;
import android.net.Uri;

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

import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 30/04/2017.
 */

public class TTSPlayer {
    private static TTSPlayer _instance;

    private OnTTSPlayCallback callback;

    private SimpleExoPlayer player;
    private DataSource.Factory dataSourceFactory;
    private ExtractorsFactory extractorsFactory;

    private File currentTTSFile;
    private boolean pausing = false;
    private int pausingPosition = 0;

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

    public void setSpeed(float speed) {
        player.setPlaybackParameters(new PlaybackParameters(speed, 1f));
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

    public interface OnTTSPlayCallback {
        void onPlayCompletion();

        void onPlayStart();

    }
}
