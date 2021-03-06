package com.tribe.app.presentation.view.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.support.annotation.StringDef;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 28/08/2016.
 */
public abstract class TribeMediaPlayer {

  public static final String GENERAL_ERROR = "error";
  public static final String FILE_NOT_FOUND_ERROR = "fileNotFound";

  @StringDef({ GENERAL_ERROR, FILE_NOT_FOUND_ERROR }) public @interface TribeMediaPlayerError {
  }

  protected Context context;
  protected String media;
  protected boolean mute;
  protected boolean looping;
  protected boolean autoStart;
  protected boolean changeSpeed;
  protected boolean isLocal;
  protected VideoSize videoSize;
  protected int audioStreamType;

  // OBSERVABLES
  PublishSubject<String> onErrorPlayer = PublishSubject.create();
  PublishSubject<Boolean> onPreparedPlayer = PublishSubject.create();
  PublishSubject<VideoSize> onVideoSizeChanged = PublishSubject.create();
  PublishSubject<Boolean> onVideoStarted = PublishSubject.create();
  PublishSubject<Boolean> onCompletion = PublishSubject.create();
  PublishSubject<Integer> onProgress = PublishSubject.create();

  public Observable<String> onErrorPlayer() {
    return onErrorPlayer;
  }

  public Observable<Boolean> onPreparedPlayer() {
    return onPreparedPlayer;
  }

  public Observable<VideoSize> onVideoSizeChanged() {
    return onVideoSizeChanged;
  }

  public Observable<Boolean> onVideoStarted() {
    return onVideoStarted;
  }

  public Observable<Boolean> onCompletion() {
    return onCompletion;
  }

  public Observable<Integer> onProgress() {
    return onProgress;
  }

  public int getAudioStreamType() {
    return audioStreamType;
  }

  protected abstract void setup();

  public abstract void setMedia(String media);

  public abstract void setSurface(SurfaceTexture surfaceTexture);

  public abstract void prepare();

  public abstract void setPlaybackRate();

  public abstract void pause();

  public abstract void play();

  public abstract void release();

  public abstract long getPosition();

  public abstract long getDuration();

  public abstract void seekTo(long position);

  public abstract int getAudioSessionId();

  public abstract boolean isPlaying();

  public static class TribeMediaPlayerBuilder {

    private final Context context;
    private final String media;
    private boolean mute = false;
    private boolean looping = false;
    private boolean autoStart = false;
    private boolean changeSpeed = false;
    private boolean forceLegacy = false;
    private boolean isLocal = false;
    private int audioStreamType = AudioManager.USE_DEFAULT_STREAM_TYPE;

    public TribeMediaPlayerBuilder(Context context, String media) {
      this.context = context;
      this.media = media;
    }

    public TribeMediaPlayerBuilder mute(boolean mute) {
      this.mute = mute;
      return this;
    }

    public TribeMediaPlayerBuilder looping(boolean looping) {
      this.looping = looping;
      return this;
    }

    public TribeMediaPlayerBuilder autoStart(boolean autoStart) {
      this.autoStart = autoStart;
      return this;
    }

    public TribeMediaPlayerBuilder canChangeSpeed(boolean changeSpeed) {
      this.changeSpeed = changeSpeed;
      return this;
    }

    public TribeMediaPlayerBuilder forceLegacy(boolean forceLegacy) {
      this.forceLegacy = forceLegacy;
      return this;
    }

    public TribeMediaPlayerBuilder isLocal(boolean isLocal) {
      this.isLocal = isLocal;
      return this;
    }

    public TribeMediaPlayerBuilder audioStreamType(int audioStreamType) {
      this.audioStreamType = audioStreamType;
      return this;
    }

    public TribeMediaPlayer build() {
      return new LegacyMediaPlayer(this);
    }

    public LegacyMediaPlayer buildLegacyMediaPlayer() {
      return new LegacyMediaPlayer(this);
    }

    public String getMedia() {
      return media;
    }

    public boolean isLooping() {
      return looping;
    }

    public boolean isMute() {
      return mute;
    }

    public boolean isAutoStart() {
      return autoStart;
    }

    public Context getContext() {
      return context;
    }

    public boolean isChangeSpeed() {
      return changeSpeed;
    }

    public boolean isLocal() {
      return isLocal;
    }

    public int getAudioStreamType() {
      return audioStreamType;
    }
  }
}
