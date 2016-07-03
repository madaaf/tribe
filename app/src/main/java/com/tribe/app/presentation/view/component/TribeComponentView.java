package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.VideoTextureView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by tiago on 10/06/2016.
 */
public class TribeComponentView extends FrameLayout implements TextureView.SurfaceTextureListener, IVLCVout.Callback {

    @BindView(R.id.viewTextureVideo)
    VideoTextureView viewTextureVideo;

    @BindView(R.id.imgAvatar)
    View imgAvatar;

    @BindView(R.id.imgSave)
    ImageView imgSave;

    @BindView(R.id.imgMore)
    ImageView imgMore;

    @BindView(R.id.txtDistance)
    TextViewFont txtDistance;

    @BindView(R.id.txtName)
    TextViewFont txtName;

    @BindView(R.id.txtTime)
    TextViewFont txtTime;

    @BindView(R.id.imgSpeed)
    ImageView imgSpeed;

    @BindView(R.id.txtSpeed)
    TextViewFont txtSpeed;

    @BindView(R.id.viewSeparator)
    View viewSeparator;

    @BindView(R.id.txtCity)
    TextViewFont txtCity;

    @BindView(R.id.txtSwipeUp)
    TextViewFont txtSwipeUp;

    @BindView(R.id.txtSwipeDown)
    TextViewFont txtSwipeDown;

    @BindView(R.id.btnBackToTribe)
    View btnBackToTribe;

    // OBSERVABLES
    private Unbinder unbinder;

    // PLAYER
    private LibVLC libVLC;
    private MediaPlayer mediaPlayer = null;
    private MediaPlayer.EventListener playerListener;
    private int videoWidth, videoHeight;
    private SurfaceTexture surfaceTexture;

    public TribeComponentView(Context context) {
        super(context);
        init(context, null);
    }

    public TribeComponentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        releasePlayer();

        try {
            ArrayList<String> options = new ArrayList<>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch");
            options.add("-vvv");

            libVLC = new LibVLC(options);

            mediaPlayer = new MediaPlayer(libVLC);
            playerListener = new PlayerListener();
            mediaPlayer.setEventListener(playerListener);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_tribe, this);
        unbinder = ButterKnife.bind(this);
        viewTextureVideo.setSurfaceTextureListener(this);
        super.onFinishInflate();
    }

    public void startPlayer() {
        Media media = new Media(libVLC, "/storage/emulated/0/Download/Tribe/Sent/c643528f-34cb-4449-ac0a-74ca6dd36142.mp4");
        mediaPlayer.setMedia(media);

        if (surfaceTexture != null) prepareWithSurface();
    }

    public void releasePlayer() {
        if (libVLC == null) return;

        mediaPlayer.stop();
        final IVLCVout vout = mediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        libVLC.release();
        libVLC = null;
    }

    public void setIconsAlpha(float alpha) {
        imgAvatar.setAlpha(alpha);
        imgSpeed.setAlpha(alpha);
        imgMore.setAlpha(alpha);
        imgSave.setAlpha(alpha);
        viewSeparator.setAlpha(alpha);
        txtCity.setAlpha(alpha);
        txtDistance.setAlpha(alpha);
        txtName.setAlpha(alpha);
        txtSpeed.setAlpha(alpha);
        txtTime.setAlpha(alpha);
    }

    public void setSwipeUpAlpha(float alpha) {
        txtSwipeUp.setAlpha(alpha);
    }

    public void setSwipeDownAlpha(float alpha) {
        txtSwipeDown.setAlpha(alpha);
    }

    public void showBackToTribe(int duration) {
        btnBackToTribe.setClickable(true);
        AnimationUtils.fadeIn(btnBackToTribe, duration);
    }

    public void hideBackToTribe(int duration) {
        btnBackToTribe.setClickable(false);
        AnimationUtils.fadeOut(btnBackToTribe, duration);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surfaceTexture = surface;
        if (mediaPlayer != null) prepareWithSurface();
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

    private void prepareWithSurface() {
        if (mediaPlayer.getMedia() != null) {
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.setVideoSurface(surfaceTexture);
            vout.addCallback(this);
            vout.attachViews();
            mediaPlayer.play();
        }
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
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }
}
