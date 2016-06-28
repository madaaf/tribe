package com.tribe.app.presentation.view.camera.recorder;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.support.design.BuildConfig;
import android.util.Log;

import com.tribe.app.R;

public class MediaVideoEncoder extends MediaEncoder {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "MediaVideoEncoder";

    // CONFIGURATION
    private static final String MIME_TYPE = "video/avc";
    private static final int FRAME_RATE = 15;
    private static final int FRAME_INTERVAL = 5;
    private static final int BITRATE = 500000;

    // VARIABLES
    private InputSurface inputSurface = null;

    // RESOURCES
    private int timeRecord;

    public MediaVideoEncoder(Context context, final TribeMuxerWrapper muxer) {
        super(muxer);

        if (DEBUG) Log.i(TAG, "MediaVideoEncoder: ");

        timeRecord = context.getResources().getInteger(R.integer.time_record);
    }

    @Override
    public void prepareEncoder(Context context, int width, int height) {
        if (mediaCodec != null || inputSurface != null) {
            throw new RuntimeException("prepareEncoder called twice?");
        }

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
            muxerStarted = isEOS = false;
        } catch (Exception e) {
            release();
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
            release();
            throw (RuntimeException) e;
        }

        return true;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void makeCurrent() {
        inputSurface.makeCurrent();
    }

    public boolean swapBuffers() {
        boolean result;
        if (result = super.frameAvailableSoon()) {
            inputSurface.swapBuffers();
            inputSurface.setPresentationTime(getPTSUs());
        }

        return result;
    }

    @Override
    protected void release() {
        if (DEBUG) Log.i(TAG, "release:");

        if (inputSurface != null) {
            inputSurface.release();
            inputSurface = null;
        }

        super.release();
    }

    @Override
    protected void signalEndOfInputStream() {
        if (DEBUG) Log.d(TAG, "sending EOS to encoder");
        mediaCodec.signalEndOfInputStream();
        isEOS = true;
    }

}
