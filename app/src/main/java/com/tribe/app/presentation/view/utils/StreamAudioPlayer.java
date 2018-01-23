package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.net.Uri;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
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
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 19/01/2018.
 */

public class StreamAudioPlayer {

  // VARIABLES
  private SimpleExoPlayer player;
  private BandwidthMeter bandwidthMeter;
  private ExtractorsFactory extractorsFactory;
  private TrackSelection.Factory trackSelectionFactory;
  private TrackSelector trackSelector;
  private DefaultBandwidthMeter defaultBandwidthMeter;
  private DataSource.Factory dataSourceFactory;
  private MediaSource mediaSource;

  // OBSERVABLES
  private PublishSubject<Boolean> onBuffered = PublishSubject.create();
  private PublishSubject<Boolean> onDonePlaying = PublishSubject.create();

  public StreamAudioPlayer(Context context) {
    bandwidthMeter = new DefaultBandwidthMeter();
    extractorsFactory = new DefaultExtractorsFactory();

    trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);

    trackSelector = new DefaultTrackSelector(trackSelectionFactory);

    defaultBandwidthMeter = new DefaultBandwidthMeter();
    dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Tribe"),
        defaultBandwidthMeter);

    player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
    player.addListener(new Player.EventListener() {
      @Override public void onTimelineChanged(Timeline timeline, Object manifest) {
      }

      @Override public void onTracksChanged(TrackGroupArray trackGroups,
          TrackSelectionArray trackSelections) {
      }

      @Override public void onLoadingChanged(boolean isLoading) {
      }

      @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_READY) {
          onBuffered.onNext(true);
        } else if (playbackState == Player.STATE_ENDED) {
          onDonePlaying.onNext(null);
        }
      }

      @Override public void onRepeatModeChanged(int repeatMode) {
      }

      @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
      }

      @Override public void onPlayerError(ExoPlaybackException error) {
      }

      @Override public void onPositionDiscontinuity(int reason) {
      }

      @Override public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
      }

      @Override public void onSeekProcessed() {
      }
    });
  }

  /**
   * PUBLIC
   */

  public void dispose() {
    player.stop();
  }

  public void prepare(String url) {
    mediaSource =
        new ExtractorMediaSource(Uri.parse(url), dataSourceFactory, extractorsFactory, null, null);
    player.prepare(mediaSource);
  }

  public void play() {
    player.setPlayWhenReady(true);
  }

  public void stop() {
    player.setPlayWhenReady(false);
  }

  public void reset() {
    player.seekTo(0);
  }

  public long getPosition() {
    return player.getCurrentPosition();
  }

  public long getDuration() {
    return player.getDuration();
  }

  /**
   * OBSERVABLE
   */

  public Observable<Boolean> onBuffered() {
    return onBuffered;
  }

  public Observable<Boolean> onDonePlaying() {
    return onDonePlaying;
  }
}
