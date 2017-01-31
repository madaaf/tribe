package com.tribe.app.presentation.view.video;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;
import android.view.Surface;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

/**
 * Created by tiago on 28/08/2016.
 */
public class ExoMediaPlayer extends TribeMediaPlayer
    implements MediaCodecVideoTrackRenderer.EventListener {

  private static final int RENDERER_COUNT = 4;
  private static final int MIN_BUFFER_MS = 250000;
  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private static final int BUFFER_SEGMENT_COUNT = 256;

  // VARIABLES
  private ExoPlayer exoPlayer = null;
  private MediaCodecVideoTrackRenderer videoRenderer;
  private MediaCodecAudioTrackRenderer audioRenderer;

  public ExoMediaPlayer(TribeMediaPlayerBuilder builder) {
    this.context = builder.getContext();
    this.media = builder.getMedia();
    this.looping = builder.isLooping();
    this.mute = builder.isMute();
    this.autoStart = builder.isAutoStart();
    this.changeSpeed = builder.isChangeSpeed();
    this.audioStreamType = builder.getAudioStreamType();

    setup();
  }

  @Override protected void setup() {
    exoPlayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
    exoPlayer.addListener(new ExoPlayer.Listener() {

      @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
          case ExoPlayer.STATE_ENDED:
            if (looping) exoPlayer.seekTo(0);
            onCompletion.onNext(true);
            break;
          case ExoPlayer.STATE_READY:
            onPreparedPlayer.onNext(true);
            if (mute) {
              exoPlayer.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0f);
            }
            if (autoStart) onVideoStarted.onNext(true);
            break;
          default:
            break;
        }
      }

      @Override public void onPlayWhenReadyCommitted() {
      }

      @Override public void onPlayerError(ExoPlaybackException error) {
        if (error != null && error.getMessage() != null && error.getMessage()
            .contains("FileNotFound")) {
          onErrorPlayer.onNext(error.toString());
        }
      }
    });

    Allocator allocator = new DefaultAllocator(MIN_BUFFER_MS);
    DataSource dataSource = new DefaultUriDataSource(context, "Android");

    ExtractorSampleSource sampleSource =
        new ExtractorSampleSource(Uri.parse(media), dataSource, allocator,
            BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE, new Mp4Extractor());

    videoRenderer =
        new MediaCodecVideoTrackRenderer(context, sampleSource, MediaCodecSelector.DEFAULT,
            MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING, 1, new Handler(), this, 1);
    audioRenderer =
        new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT, null, true, null,
            null, null, getAudioStreamType());
    exoPlayer.prepare(videoRenderer, audioRenderer);
    exoPlayer.setPlayWhenReady(autoStart);
  }

  @Override public void setMedia(String media) {
    this.media = media;
    release();
    setup();
  }

  @Override public void setSurface(SurfaceTexture surfaceTexture) {
    exoPlayer.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE,
        new Surface(surfaceTexture));
  }

  @Override public void prepare() {

  }

  @Override public void pause() {
    exoPlayer.setPlayWhenReady(false);
  }

  @Override public void play() {
    exoPlayer.setPlayWhenReady(true);
  }

  @Override public void release() {
    if (exoPlayer == null) return;

    exoPlayer.stop();

    new Thread(() -> {
      try {
        exoPlayer.release();
      } catch (IllegalStateException ex) {
        ex.printStackTrace();
      }
    }).start();
  }

  @Override public void setPlaybackRate() {

  }

  @Override public long getPosition() {
    return exoPlayer.getCurrentPosition();
  }

  @Override public void seekTo(long position) {
    exoPlayer.seekTo(position);
  }

  @Override public int getAudioSessionId() {
    return -1;
  }

  @Override public long getDuration() {
    return exoPlayer.getDuration();
  }

  @Override public boolean isPlaying() {
    return false;
  }

  @Override public void onDroppedFrames(int count, long elapsed) {
  }

  @Override public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
      float pixelWidthHeightRatio) {
    videoSize = new VideoSize(width, height);
    onVideoSizeChanged.onNext(videoSize);
  }

  @Override public void onDrawnToSurface(Surface surface) {
  }

  @Override public void onDecoderInitializationError(
      MediaCodecTrackRenderer.DecoderInitializationException e) {
  }

  @Override public void onCryptoError(MediaCodec.CryptoException e) {
  }

  @Override public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs,
      long initializationDurationMs) {
  }
}
