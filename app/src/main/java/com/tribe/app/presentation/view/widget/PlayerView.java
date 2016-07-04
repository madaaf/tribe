package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.widget.roundedtextureview.RoundedTextureView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by tiago on 17/02/2016.
 */
public class PlayerView extends FrameLayout implements IVLCVout.Callback, RoundedTextureView.SurfaceProvider {

    @Inject LibVLC libVLC;

    @BindView(R.id.roundedTextureView)
    RoundedTextureView roundedTextureView;

    // VARIABLES
    private String pathToVideo;
    private MediaPlayer mediaPlayer = null;
    private int videoWidth;
    private int videoHeight;
    private MediaPlayer.EventListener playerListener;
    private SurfaceTexture surfaceTexture;

    // OBSERVABLES
    private Unbinder unbinder;

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

        roundedTextureView.setSurfaceProvider(this);
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
        this.pathToVideo = pathToVideo;
        releasePlayer();

        try {
            mediaPlayer = new MediaPlayer(libVLC);
            playerListener = new PlayerListener();
            mediaPlayer.setEventListener(playerListener);

            Media m = new Media(libVLC, pathToVideo);
            mediaPlayer.setMedia(m);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    public void showPlayer() {
        roundedTextureView.setVisibility(View.VISIBLE);
        if (surfaceTexture != null) prepareWithSurface();
    }

    public void releasePlayer() {
        if (mediaPlayer == null) return;

        mediaPlayer.stop();
        final IVLCVout vout = mediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();

        videoWidth = 0;
        videoHeight = 0;
    }

    public void hideVideo() {
        roundedTextureView.setVisibility(View.GONE);
    }

    @Override
    public void onSurfaceCreated(SurfaceTexture surface) {
        surfaceTexture = surface;
        if (mediaPlayer != null) {
            prepareWithSurface();
        }
    }

    private void prepareWithSurface() {
        final IVLCVout vout = mediaPlayer.getVLCVout();
        vout.setVideoSurface(surfaceTexture);
        vout.addCallback(this);
        vout.attachViews();
        mediaPlayer.play();
    }

    private class PlayerListener implements MediaPlayer.EventListener {

        public PlayerListener() {

        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    mediaPlayer.setPosition(0);
                    mediaPlayer.play();
                    break;
                case MediaPlayer.Event.Vout:
                    mediaPlayer.setVolume(0);
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