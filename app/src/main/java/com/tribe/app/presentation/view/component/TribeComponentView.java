package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.AssetDataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.tribe.app.R;
import com.tribe.app.presentation.view.listener.MediaCodecVideoListener;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.VideoTextureView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by tiago on 10/06/2016.
 */
public class TribeComponentView extends FrameLayout implements TextureView.SurfaceTextureListener {

    private static final int RENDERER_COUNT = 300000;
    private static final int MIN_BUFFER_MS = 250000;
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;

    @BindView(R.id.surfaceView)
    VideoTextureView surfaceView;

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

    // VARIABLES
    private ExoPlayer exoPlayer;

    public TribeComponentView(Context context) {
        super(context);
        init(context, null);
    }

    public TribeComponentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
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
        surfaceView.setSurfaceTextureListener(this);
        super.onFinishInflate();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        initPlayer();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void initPlayer() {
        String url = "asset:///binary.mp4";
        Allocator allocator = new DefaultAllocator(MIN_BUFFER_MS);
        AssetDataSource dataSource = new AssetDataSource(getContext());

        ExtractorSampleSource sampleSource = new ExtractorSampleSource(Uri.parse(url), dataSource, allocator,
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE, new Mp4Extractor());

        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(
                getContext(), sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT,
                1, new Handler(), new MediaCodecVideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                surfaceView.setVideoWidthHeightRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
                surfaceView.setScalingMode(VideoTextureView.ScalingMode.CROP);
            }
        }, 1);

        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT);

        exoPlayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT);
        exoPlayer.prepare(videoRenderer, audioRenderer);
        exoPlayer.addListener(new ExoPlayer.Listener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch(playbackState) {
                    case ExoPlayer.STATE_ENDED:
                        exoPlayer.seekTo(0);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPlayWhenReadyCommitted() {}

            @Override
            public void onPlayerError(ExoPlaybackException error) {}
        });
        exoPlayer.seekTo(0);
        exoPlayer.sendMessage(videoRenderer,
                MediaCodecVideoTrackRenderer.MSG_SET_SURFACE,
                new Surface(surfaceView.getSurfaceTexture()));
        exoPlayer.setPlayWhenReady(false);
    }

    public void startPlayer() {
        if (surfaceView.getSurfaceTexture() != null)
            exoPlayer.setPlayWhenReady(true);
    }

    public void release() {
        exoPlayer.stop();
        exoPlayer.release();
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
}
