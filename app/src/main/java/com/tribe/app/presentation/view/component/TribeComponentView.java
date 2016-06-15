package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

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
import com.tribe.app.presentation.view.widget.VideoSurfaceView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by tiago on 10/06/2016.
 */
public class TribeComponentView extends FrameLayout {

    private static final int RENDERER_COUNT = 300000;
    private static final int MIN_BUFFER_MS = 250000;
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;

    @BindView(R.id.surfaceView)
    VideoSurfaceView surfaceView;

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
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_tribe, this);
        unbinder = ButterKnife.bind(this);

        initExoPlayer();
    }

    private void initExoPlayer() {
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
                surfaceView.setScalingMode(VideoSurfaceView.ScalingMode.CROP);
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
                surfaceView.getHolder().getSurface());
        exoPlayer.setPlayWhenReady(true);
    }

    public void release() {
        exoPlayer.stop();
        exoPlayer.release();
    }
}
