package com.tribe.app.presentation.view.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.annotation.StringDef;

import com.tribe.app.presentation.view.utils.DeviceUtils;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 28/08/2016.
 */
public abstract class TribeMediaPlayer {

    public static final String ERROR = "error";

    @StringDef({ERROR})
    public @interface TribeMediaPlayerError {}

    protected Context context;
    protected String media;
    protected boolean mute;
    protected boolean looping;
    protected boolean autoStart;
    protected VideoSize videoSize;

    // OBSERVABLES
    PublishSubject<String> onErrorPlayer = PublishSubject.create();
    PublishSubject<Boolean> onPreparedPlayer = PublishSubject.create();
    PublishSubject<VideoSize> onVideoSizeChanged = PublishSubject.create();
    PublishSubject<Boolean> onVideoStarted = PublishSubject.create();

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

    protected abstract void setup();
    public abstract void setMedia(String media);
    public abstract void setSurface(SurfaceTexture surfaceTexture);
    public abstract void pausePlayer();
    public abstract void resumePlayer();
    public abstract void releasePlayer();

    public static class TribeMediaPlayerBuilder {
        private final Context context;
        private final String media;
        private boolean mute = false;
        private boolean looping = false;
        private boolean autoStart = false;

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

        public TribeMediaPlayer build() {
            return !DeviceUtils.supportsExoPlayer(context) ? new LegacyMediaPlayer(this) : new ExoMediaPlayer(this);
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
    }
}
