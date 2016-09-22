package com.tribe.app.presentation.view.video;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.view.Surface;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.scope.SpeedPlayback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.inject.Inject;

/**
 * Created by tiago on 28/08/2016.
 */
public class LegacyMediaPlayer extends TribeMediaPlayer implements MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    @Inject
    @SpeedPlayback
    Preference<Float> speedPlayback;

    // VARIABLES
    private MediaPlayer mediaPlayer = null;

    public LegacyMediaPlayer(TribeMediaPlayerBuilder builder) {
        this.context = builder.getContext();
        this.media = builder.getMedia();
        this.looping = builder.isLooping();
        this.mute = builder.isMute();
        this.autoStart = builder.isAutoStart();
        this.changeSpeed = builder.isChangeSpeed();
        this.isLocal = builder.isLocal();

        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

        setup();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        onErrorPlayer.onNext(ERROR);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        onPreparedPlayer.onNext(true);

        if (autoStart) {
            play();
            onVideoStarted.onNext(true);
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        videoSize = new VideoSize(width, height);
        onVideoSizeChanged.onNext(videoSize);
    }

    @Override
    protected void setup() {
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
            if (mute) mediaPlayer.setVolume(0, 0);
            mediaPlayer.setLooping(looping);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setMedia(String media) {
        this.media = media;
        setup();
    }

    @Override
    public void setSurface(SurfaceTexture surfaceTexture) {
        mediaPlayer.setSurface(new Surface(surfaceTexture));

        try {
            if (!isLocal) {
                RandomAccessFile raf = new RandomAccessFile(media, "r");
                mediaPlayer.setDataSource(raf.getFD(), 0, raf.length());
            } else {
                mediaPlayer.setDataSource(context, Uri.parse(media));
            }

            mediaPlayer.prepareAsync();
        } catch (IllegalStateException ex) {
            try {
                mediaPlayer.reset();
                RandomAccessFile raf = new RandomAccessFile(media, "r");
                mediaPlayer.setDataSource(raf.getFD());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        mediaPlayer.stop();
    }

    @Override
    public void play() {
        mediaPlayer.start();
        setPlaybackRate();
    }

    @Override
    public void release() {
        if (mediaPlayer == null) return;

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

        videoSize = null;
    }

    @Override
    public void setPlaybackRate() {
        if (android.os.Build.VERSION.SDK_INT >= 23 && mediaPlayer != null) {
            try {
                PlaybackParams myPlayBackParams = new PlaybackParams();
                myPlayBackParams.setSpeed(speedPlayback.get());
                mediaPlayer.setPlaybackParams(myPlayBackParams);
            } catch (IllegalStateException ex) {}
        }
    }

    @Override
    public long getPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(long position) {
        mediaPlayer.seekTo((int) position);
    }

    @Override
    public long getDuration() {
        return mediaPlayer.getDuration();
    }
}
