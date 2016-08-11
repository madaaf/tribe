package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 17/02/2016.
 */
public class PlayerView extends FrameLayout implements IVLCVout.Callback, TextureView.SurfaceTextureListener {

    @Inject LibVLC libVLC;

    @BindView(R.id.textureViewLayout)
    CardView textureViewLayout;

    // VARIABLES
    private VideoTextureView videoTextureView;
    private String pathToVideo;
    private MediaPlayer mediaPlayer = null;
    private int videoWidth;
    private int videoHeight;
    private MediaPlayer.EventListener playerListener;
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

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
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
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        this.releasePlayer();
        Toast.makeText(getContext(), "Error with hardware acceleration", Toast.LENGTH_LONG).show();
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
            System.out.println("HEY CREATE PLAYER");
            mediaPlayer = new MediaPlayer(libVLC);
            playerListener = new PlayerListener();
            mediaPlayer.setEventListener(playerListener);

            //RandomAccessFile raf = new RandomAccessFile(pathToVideo, "r");
            Media m = new Media(libVLC, pathToVideo);
            mediaPlayer.setMedia(m);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    public void releasePlayer() {
        if (mediaPlayer == null) return;

        new Thread(() -> {
            try {
                mediaPlayer.stop();
                final IVLCVout vout = mediaPlayer.getVLCVout();
                vout.removeCallback(PlayerView.this);
                vout.detachViews();
                mediaPlayer.release();
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
        mediaPlayer.play();
    }

    private void prepareWithSurface() {
        final IVLCVout vout = mediaPlayer.getVLCVout();
        vout.setVideoSurface(surfaceTexture);
        vout.addCallback(this);
        vout.attachViews();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        surfaceTexture = surface;

        System.out.println("HEY SURFACE AVAILABLE");
        if (mediaPlayer != null) {
            System.out.println("HEY SURFACE + PLAYER");
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

    private class PlayerListener implements MediaPlayer.EventListener {

        public PlayerListener() {

        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    break;
                case MediaPlayer.Event.Vout:
                    if (mediaPlayer != null && mediaPlayer.getVLCVout() != null)
                        mediaPlayer.setVolume(0);

                    if (!hasSentStarted) {
                        videoStarted.onNext(PlayerView.this);
                        hasSentStarted = true;
                    }
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }
}