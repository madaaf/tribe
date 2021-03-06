package com.tribe.app.presentation.view.video;

import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.view.Surface;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tiago on 28/08/2016.
 */
public class LegacyMediaPlayer extends TribeMediaPlayer
    implements MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

  // VARIABLES
  private MediaPlayer mediaPlayer = null;
  private Subscription timerCompletionSubscription;
  private Subscription timerProgressSubscription;
  private boolean isPrepared = false;

  public LegacyMediaPlayer(TribeMediaPlayerBuilder builder) {
    this.context = builder.getContext();
    this.media = builder.getMedia();
    this.looping = builder.isLooping();
    this.mute = builder.isMute();
    this.autoStart = builder.isAutoStart();
    this.changeSpeed = builder.isChangeSpeed();
    this.isLocal = builder.isLocal();
    this.audioStreamType = builder.getAudioStreamType();

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    setup();
  }

  @Override public boolean onError(MediaPlayer mp, int what, int extra) {
    return false;
  }

  @Override public void onPrepared(MediaPlayer mp) {
    onPreparedPlayer.onNext(true);
    mediaPlayer.seekTo(0);
    isPrepared = true;

    if (autoStart) {
      play();
    }
  }

  @Override public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    videoSize = new VideoSize(width, height);
    onVideoSizeChanged.onNext(videoSize);
  }

  @Override protected void setup() {
    isPrepared = false;

    try {
      if (mediaPlayer != null) {
        mediaPlayer.reset();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
      }

      mediaPlayer = new MediaPlayer();
      mediaPlayer.setOnVideoSizeChangedListener(this);
      mediaPlayer.setOnPreparedListener(this);
      mediaPlayer.setOnErrorListener(this);

      if (mute) {
        mediaPlayer.setVolume(0.0f, 0.0f);
      } else {
        mediaPlayer.setVolume(1.0f, 1.0f);
      }
      mediaPlayer.setLooping(looping);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override public void setMedia(String media) {
    this.media = media;
    setup();
  }

  @Override public void setSurface(SurfaceTexture surfaceTexture) {
    if (surfaceTexture == null) {
      mediaPlayer.setDisplay(null);
      mediaPlayer.setSurface(null);
    } else {
      mediaPlayer.setSurface(new Surface(surfaceTexture));
    }
  }

  @Override public void prepare() {
    if (isPrepared) {
      if (mediaPlayer != null) mediaPlayer.seekTo(0);
      return;
    }

    try {
      if (!isLocal) {
        RandomAccessFile raf = new RandomAccessFile(media, "r");
        mediaPlayer.setDataSource(raf.getFD(), 0, raf.length());
        mediaPlayer.setAudioStreamType(getAudioStreamType());
      } else if (!StringUtils.isEmpty(media) && media.contains("asset")) { // TODO BETTER
        AssetFileDescriptor afd = context.getAssets().openFd("video/walkthrough.mp4");
        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();
      }

      mediaPlayer.prepareAsync();
    } catch (IllegalStateException ex) {
      ex.printStackTrace();
      try {
        mediaPlayer.reset();
        RandomAccessFile raf = new RandomAccessFile(media, "r");
        mediaPlayer.setDataSource(raf.getFD());
        mediaPlayer.setAudioStreamType(getAudioStreamType());
        mediaPlayer.prepareAsync();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (FileNotFoundException e) {
      onErrorPlayer.onNext(FILE_NOT_FOUND_ERROR);
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override public void pause() {
    mediaPlayer.pause();

    if (timerCompletionSubscription != null) {
      timerCompletionSubscription.unsubscribe();
      timerCompletionSubscription = null;
    }

    if (timerProgressSubscription != null) {
      timerProgressSubscription.unsubscribe();
      timerProgressSubscription = null;
    }
  }

  @Override public void play() {
    mediaPlayer.start();
    onVideoStarted.onNext(true);
    setPlaybackRate();
    scheduleTimerCompletion();
    scheduleTimerProgress();
  }

  @Override public void release() {
    if (mediaPlayer == null) return;

    isPrepared = false;

    new Thread(() -> {
      try {
        if (mediaPlayer != null) {
          mediaPlayer.reset();
          mediaPlayer.stop();
          mediaPlayer.release();
          mediaPlayer = null;
        }
      } catch (IllegalStateException ex) {
        ex.printStackTrace();
      }
    }).start();

    if (timerCompletionSubscription != null) {
      timerCompletionSubscription.unsubscribe();
      timerCompletionSubscription = null;
    }

    if (timerProgressSubscription != null) {
      timerProgressSubscription.unsubscribe();
      timerProgressSubscription = null;
    }

    videoSize = null;
  }

  @Override public void setPlaybackRate() {
    if (android.os.Build.VERSION.SDK_INT >= 23 && mediaPlayer != null) {
      try {
        PlaybackParams myPlayBackParams = new PlaybackParams();
        mediaPlayer.setPlaybackParams(myPlayBackParams);
        scheduleTimerCompletion();
        scheduleTimerProgress();
      } catch (IllegalStateException ex) {
      }
    }
  }

  @Override public long getPosition() {
    return mediaPlayer.getCurrentPosition();
  }

  @Override public void seekTo(long position) {
    mediaPlayer.seekTo((int) position);
  }

  @Override public int getAudioSessionId() {
    return mediaPlayer.getAudioSessionId();
  }

  @Override public long getDuration() {
    return mediaPlayer.getDuration();
  }

  @Override public void onCompletion(MediaPlayer mp) {
    //onCompletion.onNext(true);
  }

  @Override public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  private void scheduleTimerCompletion() {
    if (isPrepared && mediaPlayer != null) {
      if (timerCompletionSubscription != null) timerCompletionSubscription.unsubscribe();

      if (mediaPlayer.getCurrentPosition() > -1 && mediaPlayer.getDuration() > -1) {
        timerCompletionSubscription =
            Observable.interval(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration(),
                TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(count -> {
                  if (count > 0) {
                    onCompletion.onNext(true); // WE DON'T SEND THE FIRST EVENT
                  }
                });
      }
    }
  }

  private void scheduleTimerProgress() {
    if (isPrepared && mediaPlayer != null) {
      if (timerProgressSubscription != null) timerProgressSubscription.unsubscribe();

      if (mediaPlayer.getCurrentPosition() > -1 && mediaPlayer.getDuration() > -1) {
        timerProgressSubscription = Observable.interval(0, 1, TimeUnit.MILLISECONDS)
            .onBackpressureDrop()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(count -> onProgress.onNext(mediaPlayer.getCurrentPosition()));
      }
    }
  }
}
