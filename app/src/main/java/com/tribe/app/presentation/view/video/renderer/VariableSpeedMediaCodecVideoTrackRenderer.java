package com.tribe.app.presentation.view.video.renderer;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.MediaClock;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;

public class VariableSpeedMediaCodecVideoTrackRenderer extends MediaCodecVideoTrackRenderer
        implements MediaClock {

    private double playbackRate = 1.0d;

    private boolean started = false;

    private long lastTimeUs = 0;

    private long currentPositionUs = 0;

    public VariableSpeedMediaCodecVideoTrackRenderer(Context context, SampleSource source,
                                                     MediaCodecSelector mediaCodecSelector,
                                                     int videoScalingMode,
                                                     long allowedJoiningTimeMs,
                                                     Handler eventHandler,
                                                     EventListener eventListener,
                                                     int maxDroppedFrameCountToNotify,
                                                     float playbackRate) {
        super(context, source, mediaCodecSelector, videoScalingMode, allowedJoiningTimeMs, null,
                false, eventHandler, eventListener, maxDroppedFrameCountToNotify);
        this.playbackRate = playbackRate;
    }

    protected void setPlaybackRate(float playbackRate) {
        this.playbackRate = playbackRate;
    }

    @Override
    protected MediaClock getMediaClock() {
        return this;
    }

    @Override
    protected void onStarted() {
        super.onStarted();
        started = true;
        lastTimeUs = SystemClock.elapsedRealtime() * 1000;
    }

    @Override
    protected void onStopped() {
        super.onStopped();
        started = false;
    }

    @Override
    protected void onDiscontinuity(long positionUs) throws ExoPlaybackException {
        super.onDiscontinuity(positionUs);
        lastTimeUs = SystemClock.elapsedRealtime() * 1000;
        currentPositionUs = positionUs;
    }

    @Override
    public long getPositionUs() {
        long currentTimeUs = SystemClock.elapsedRealtime() * 1000;

        if (!started) {
            lastTimeUs = currentTimeUs;
            return currentPositionUs;
        }

        // Calculate time passed
        long deltaUs = currentTimeUs - lastTimeUs;

        lastTimeUs = currentTimeUs;

        // Multiply by playback rate to get video time passed and add to current position
        currentPositionUs += Math.round(deltaUs * playbackRate);

        return currentPositionUs;
    }

}