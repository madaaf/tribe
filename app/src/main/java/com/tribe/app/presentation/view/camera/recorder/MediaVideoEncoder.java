package com.tribe.app.presentation.view.camera.recorder;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import com.tribe.app.R;

public class MediaVideoEncoder extends MediaEncoder {

    private static final boolean DEBUG = false;
    private static final String TAG = "MediaVideoEncoder";

    // CONFIGURATION
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    private static final int FRAME_RATE = 15;
    private static final int FRAME_INTERVAL = 10;
    private static final int BITRATE = 900000;

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

        int endWidth = (int) ((float) width / 16) * 16;
        int endHeight = (int) (endWidth / ((float) width / height));

        try {
            MediaFormat format = MediaFormat.createVideoFormat(
                    MIME_TYPE,
                    endWidth,
                    endHeight);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BITRATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, FRAME_INTERVAL);

            final MediaCodecInfo videoCodecInfo = selectVideoCodec(MIME_TYPE);

            if (videoCodecInfo == null) {
                throw new IllegalArgumentException("Unable to find an appropriate codec for " + MIME_TYPE);
            }

            if (DEBUG) Log.i(TAG, "selected codec: " + videoCodecInfo.getName());

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
            isEncoding = true;
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
        if (result = super.frameAvailableSoon() && inputSurface != null) {
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
        try {
            if (isEncoding)
                mediaCodec.signalEndOfInputStream();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }

        isEOS = true;
    }

    /**
     * select the first codec that match a specific MIME type
     * @param mimeType
     * @return null if no codec matched
     */
    @SuppressWarnings("deprecation")
    protected static final MediaCodecInfo selectVideoCodec(final String mimeType) {
        if (DEBUG) Log.v(TAG, "selectVideoCodec:");

        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder() && !codecInfo.getName().equals("OMX.google.h264.encoder")) {	// skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color format
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (DEBUG) Log.i(TAG, "codec:" + codecInfo.getName() + ",MIME=" + types[j]);
                    final int format = selectColorFormat(codecInfo, mimeType);
                    if (format > 0) {
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * select color format available on specific codec and we can use.
     * @return 0 if no colorFormat is matched
     */
    protected static final int selectColorFormat(final MediaCodecInfo codecInfo, final String mimeType) {
        if (DEBUG) Log.i(TAG, "selectColorFormat: ");
        int result = 0;
        final MediaCodecInfo.CodecCapabilities caps;
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            caps = codecInfo.getCapabilitiesForType(mimeType);
        } finally {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
            colorFormat = caps.colorFormats[i];
            if (isRecognizedVideoFormat(colorFormat)) {
                if (result == 0)
                    result = colorFormat;
                break;
            }
        }
        if (result == 0)
            if (DEBUG) Log.e(TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return result;
    }

    /**
     * color formats that we can use in this class
     */
    protected static int[] recognizedFormats;
    static {
        recognizedFormats = new int[] {
//        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
//        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
//        	MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };
    }

    protected static final boolean isRecognizedVideoFormat(final int colorFormat) {
        if (DEBUG) Log.i(TAG, "isRecognizedViewoFormat:colorFormat=" + colorFormat);
        final int n = recognizedFormats != null ? recognizedFormats.length : 0;
        for (int i = 0; i < n; i++) {
            if (recognizedFormats[i] == colorFormat) {
                return true;
            }
        }
        return false;
    }
}
