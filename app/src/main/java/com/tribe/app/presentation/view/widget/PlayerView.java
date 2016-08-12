package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 17/02/2016.
 */
public class PlayerView extends FrameLayout implements TextureView.SurfaceTextureListener,
        MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    @BindView(R.id.textureViewLayout)
    CardView textureViewLayout;

    // VARIABLES
    private VideoTextureView videoTextureView;
    private String pathToVideo;
    private MediaPlayer mediaPlayer = null;
    private int videoWidth;
    private int videoHeight;
    private SurfaceTexture surfaceTexture;
    private boolean hasSentStarted = false;

    // OBSERVABLES
    private Unbinder unbinder;
    private final PublishSubject<View> videoStarted = PublishSubject.create();

    public PlayerView(Context context) {
        this(context, null);
        init(context, null);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_player, this, true);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void createPlayer(String pathToVideo) {
        videoTextureView = new VideoTextureView(getContext());
        CardView.LayoutParams params = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        textureViewLayout.addView(videoTextureView, params);
        videoTextureView.setScaleType(ScalableTextureView.ScaleType.CENTER_CROP);
        videoTextureView.setSurfaceTextureListener(this);

        this.pathToVideo = pathToVideo;
        hasSentStarted = false;

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
            mediaPlayer.setVolume(0, 0);
            mediaPlayer.setLooping(true);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    public void releasePlayer() {
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

        videoHeight = 0;
        videoWidth = 0;
    }

    public void hideVideo() {
        releasePlayer();
        surfaceTexture = null;
        textureViewLayout.removeView(videoTextureView);
        videoTextureView = null;
    }

    public void play() {
        //mediaPlayer.start();
    }

    private void prepareWithSurface() {
        Surface s = new Surface(surfaceTexture);
        mediaPlayer.setSurface(s);

        try {
            RandomAccessFile raf = new RandomAccessFile(pathToVideo, "r");
            mediaPlayer.setDataSource(raf.getFD(), 0, raf.length());
            mediaPlayer.prepareAsync();
        } catch (IllegalStateException ex) {
            try {
                mediaPlayer.reset();
                RandomAccessFile raf = new RandomAccessFile(pathToVideo, "r");
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
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (width * height == 0)
            return;

        videoWidth = width;
        videoHeight = height;

        if (videoTextureView != null && videoTextureView.getContentHeight() != height) {
            videoTextureView.setContentWidth(width);
            videoTextureView.setContentHeight(height);
            videoTextureView.updateTextureViewSize();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        videoStarted.onNext(this);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surfaceTexture = surface;

        if (mediaPlayer != null) {
            prepareWithSurface();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public Observable<View> videoStarted() {
        return videoStarted;
    }
}