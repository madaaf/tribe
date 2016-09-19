package com.tribe.app.presentation.view.camera.recorder;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.support.design.BuildConfig;
import android.util.Log;

import com.tribe.app.presentation.utils.FileUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TribeMuxerWrapper {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "MediaMuxerWrapper";

    // VARIABLES
    private String outputPath;
    private final MediaMuxer mediaMuxer;
    private int encoderCount, startedCount;
    private boolean isStarted;
    private MediaEncoder videoEncoder, audioEncoder;

    public TribeMuxerWrapper(String fileId) throws IOException {
        outputPath = FileUtils.generateFile(fileId, FileUtils.VIDEO);
        mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        encoderCount = startedCount = 0;
        isStarted = false;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void prepare(Context context, int width, int height) throws IOException {
        if (videoEncoder != null)
            videoEncoder.prepareEncoder(context, width, height);
        
        if (audioEncoder != null)
            audioEncoder.prepareEncoder(context, width, height);
    }

    public void startRecording() {
        if (videoEncoder != null)
            videoEncoder.startRecording();

        if (audioEncoder != null)
            audioEncoder.startRecording();
    }

    public void stopRecording() {
        if (videoEncoder != null) {
            videoEncoder.stopRecording();
            videoEncoder = null;
        }

        if (audioEncoder != null) {
            audioEncoder.stopRecording();
            audioEncoder = null;
        }
    }

    public synchronized boolean isStarted() {
        return isStarted;
    }

    public void addEncoder(final MediaEncoder encoder) {
        if (encoder instanceof MediaVideoEncoder) {
            if (videoEncoder != null)
                throw new IllegalArgumentException("Video encoder already added.");

            videoEncoder = encoder;
        } else if (encoder instanceof MediaAudioEncoder) {
            if (audioEncoder != null)
                throw new IllegalArgumentException("Video encoder already added.");

            audioEncoder = encoder;
        } else {
            throw new IllegalArgumentException("unsupported encoder");
        }

        encoderCount = (videoEncoder != null ? 1 : 0) + (audioEncoder != null ? 1 : 0);
    }

    public synchronized boolean start() {
        if (DEBUG) Log.v(TAG, "start:");

        startedCount++;

        if ((encoderCount > 0) && (startedCount == encoderCount)) {
            mediaMuxer.start();
            isStarted = true;
            notifyAll();
            if (DEBUG) Log.v(TAG, "MediaMuxer started:");
        }

        return isStarted;
    }

    public synchronized void stop() {
        if (DEBUG) Log.v(TAG, "stop:startedCount=" + startedCount);

        startedCount--;

        if ((encoderCount > 0) && (startedCount <= 0)) {
            mediaMuxer.stop();
            mediaMuxer.release();
            isStarted = false;
            if (DEBUG) Log.v(TAG, "MediaMuxer stopped:");
        }
    }

    public synchronized int addTrack(final MediaFormat format) {
        if (isStarted)
            throw new IllegalStateException("muxer already started");

        final int trackIndex = mediaMuxer.addTrack(format);

        if (DEBUG)
            Log.i(TAG, "addTrack:trackNum=" + encoderCount + ",trackId=" + trackIndex + ",format=" + format);

        return trackIndex;
    }

    public synchronized void writeSampleData(final int trackIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo) {
        if (startedCount > 0)
            mediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }
}
