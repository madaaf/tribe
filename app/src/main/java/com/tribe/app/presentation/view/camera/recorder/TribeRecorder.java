package com.tribe.app.presentation.view.camera.recorder;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.tribe.app.R;

import java.nio.ByteBuffer;

public class TribeRecorder {

    private static final String MIME_TYPE = "video/avc";
    private static final int FRAME_RATE = 15;
    private static final int FRAME_INTERVAL = 5;
    private static final int BITRATE = 500000;

    // VARIABLES
    private MediaCodec mediaCodec = null;
    private MediaMuxer mediaMuxer = null;
    private InputSurface inputSurface = null;
    private MediaCodec.BufferInfo bufferInfo = null;
    private int trackIndex = -1;
    private boolean muxerStarted = false;
    private int totalSize = 0;

    // RESOURCES
    private int timeRecord;

    public TribeRecorder(Context context) {
        timeRecord = context.getResources().getInteger(R.integer.time_record);
    }

    public void prepareEncoder(Context context, String filePath, int width, int height) {
        if (mediaCodec != null || inputSurface != null) {
            throw new RuntimeException("prepareEncoder called twice?");
        }

        bufferInfo = new MediaCodec.BufferInfo();

        try {
            MediaFormat format = MediaFormat.createVideoFormat(
                    MIME_TYPE,
                    width,
                    height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BITRATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, FRAME_INTERVAL);

            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaMuxer = new MediaMuxer(filePath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            muxerStarted = false;
        } catch (Exception e) {
            releaseEncoder();
            throw (RuntimeException) e;
        }
    }

    public boolean firstTimeSetup() {
        if (!isRecording() || inputSurface != null) {
            return false;
        }

        try {
            inputSurface = new InputSurface(mediaCodec.createInputSurface());
            mediaCodec.start();
        } catch (Exception e) {
            releaseEncoder();
            throw (RuntimeException) e;
        }

        return true;
    }

    public boolean isRecording() {
        return mediaCodec != null;
    }

    public void makeCurrent() {
        inputSurface.makeCurrent();
    }

    synchronized public void swapBuffers() {
        if (!isRecording()) {
            return;
        }

        drainEncoder(false);
        inputSurface.swapBuffers();
        inputSurface.setPresentationTime(System.nanoTime());
    }

    synchronized public void stop() {
        drainEncoder(true);
        releaseEncoder();
    }

    private void releaseEncoder() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }

        if (inputSurface != null) {
            inputSurface.release();
            inputSurface = null;
        }

        if (mediaMuxer != null) {
            mediaMuxer.stop();
            mediaMuxer.release();
            mediaMuxer = null;
        }
    }

    private void drainEncoder(boolean endOfStream) {
        if (endOfStream) {
            mediaCodec.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mediaCodec.getOutputBuffers();

        while (true) {
            int encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = mediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (muxerStarted) {
                    throw new RuntimeException("format changed twice");
                }

                MediaFormat newFormat = mediaCodec.getOutputFormat();
                trackIndex = mediaMuxer.addTrack(newFormat);
                mediaMuxer.start();
                muxerStarted = true;
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];

                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0) {
                    if (!muxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);

                    boolean calcTime = true;

                    if (calcTime) {
                        long t0 = System.currentTimeMillis();
                        mediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                        totalSize += bufferInfo.size;
                        long dt = System.currentTimeMillis() - t0;
                        if (dt > 50)
                            Log.e("DEBUG", String.format("XXX: dt=%d, size=%.2f", dt, (float) totalSize / 1024 / 1024));
                    } else {
                        mediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                    }
                }

                mediaCodec.releaseOutputBuffer(encoderStatus, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }
            }
        }
    }
}
