package com.tribe.app.presentation.view.camera.recorder;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import rx.Observable;
import rx.subjects.PublishSubject;

public abstract class MediaEncoder implements Runnable {

    private static final boolean DEBUG = true;
    private static final String TAG = "MediaEncoder";

    protected static final int TIMEOUT_USEC = 10000;
    protected static final int MSG_FRAME_AVAILABLE = 1;
    protected static final int MSG_STOP_RECORDING = 2;

    // OBSERVABLE
    protected PublishSubject<MediaEncoder> onPreparedSubject = PublishSubject.create();
    protected PublishSubject<MediaEncoder> onEndSubject = PublishSubject.create();

    // VARIABLES
    protected final Object sync = new Object();
    protected volatile boolean isRecording = false;
    protected volatile boolean isEncoding = false;
    private int requestDrain;
    protected volatile boolean requestStop;
    protected boolean isEOS;
    protected boolean muxerStarted;
    protected int trackIndex;
    protected MediaCodec mediaCodec;
    protected final WeakReference<TribeMuxerWrapper> weakMuxer;
    protected MediaCodec.BufferInfo bufferInfo;

    public MediaEncoder(final TribeMuxerWrapper muxer) {
        if (muxer == null) throw new NullPointerException("TribeMuxerWrapper is null");

        weakMuxer = new WeakReference<>(muxer);
        muxer.addEncoder(this);

        synchronized (sync) {
            bufferInfo = new MediaCodec.BufferInfo();

            new Thread(this, getClass().getSimpleName()).start();

            try {
                sync.wait();
            } catch (final InterruptedException e) { }
        }
    }

    public String getOutputPath() {
        final TribeMuxerWrapper muxer = weakMuxer.get();
        return muxer != null ? muxer.getOutputPath() : null;
    }

    public boolean frameAvailableSoon() {
        synchronized (sync) {
            if (!isRecording || requestStop) {
                return false;
            }

            requestDrain++;
            sync.notifyAll();
        }
        return true;
    }

    @Override
    public void run() {
        synchronized (sync) {
            requestStop = false;
            requestDrain = 0;
            sync.notify();
        }

        final boolean isRunning = true;
        boolean localRequestStop;
        boolean localRequestDrain;

        while (isRunning) {
            synchronized (sync) {
                localRequestStop = requestStop;
                localRequestDrain = (requestDrain > 0);

                if (localRequestDrain)
                    requestDrain--;
            }

            if (localRequestStop) {
                drain();
                signalEndOfInputStream();
                drain();
                release();
                break;
            }
            if (localRequestDrain) {
                drain();
            } else {
                synchronized (sync) {
                    try {
                        sync.wait();
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
            }
        }

        if (DEBUG) Log.d(TAG, "Encoder thread exiting");

        synchronized (sync) {
            requestStop = true;
            isRecording = false;
        }
    }

    public abstract void prepareEncoder(Context context, int width, int height);

    public void startRecording() {
        if (DEBUG) Log.v(TAG, "startRecording");
        synchronized (sync) {
            System.out.println("START RECORDING");
            isRecording = true;
            requestStop = false;
            sync.notifyAll();
        }
    }

    public void stopRecording() {
        if (DEBUG) Log.v(TAG, "stopRecording");

        System.out.println("STOP RECORDING");

        synchronized (sync) {
            if (!isRecording || requestStop) {
                return;
            }

            requestStop = true;
            sync.notifyAll();
        }
    }

    protected void release() {
        if (DEBUG) Log.d(TAG, "release:");

        try {
            onEndSubject.onNext(this);
        } catch (final Exception e) {
            Log.e(TAG, "failed onStopped", e);
        }

        isRecording = false;

        if (mediaCodec != null) {
            try {
                isEncoding = false;
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            } catch (final Exception e) {
                Log.e(TAG, "failed releasing MediaCodec", e);
            }
        }

        if (muxerStarted) {
            final TribeMuxerWrapper muxer = weakMuxer != null ? weakMuxer.get() : null;
            if (muxer != null) {
                try {
                    muxer.stop();
                } catch (final Exception e) {
                    Log.e(TAG, "failed stopping muxer", e);
                }
            }
        }

        bufferInfo = null;
    }

    protected void signalEndOfInputStream() {
        if (DEBUG) Log.d(TAG, "sending EOS to encoder");
        encode(null, 0, getPTSUs() / 1000);
    }

    /**
     * Method to set byte array to the MediaCodec encoder
     *
     * @param buffer
     * @param length             　length of byte array, zero means EOS.
     * @param presentationTimeUs
     */
    protected void encode(final ByteBuffer buffer, final int length, final long presentationTimeUs) {
        if (!isRecording) return;

        final ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();

        while (isRecording) {
            final int inputBufferIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC);

            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (buffer != null) {
                    inputBuffer.put(buffer);
                }

                if (length <= 0) {
                    isEOS = true;
                    if (DEBUG) Log.i(TAG, "send BUFFER_FLAG_END_OF_STREAM");
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, length,
                            presentationTimeUs, 0);
                }

                break;
            }
        }
    }

    protected void drain() {
        if (mediaCodec == null) return;

        try {
            ByteBuffer[] encoderOutputBuffers = mediaCodec.getOutputBuffers();
            int encoderStatus;
            final TribeMuxerWrapper muxer = weakMuxer.get();

            if (muxer == null) {
                Log.w(TAG, "muxer is unexpectedly null");
                return;
            }

            while (isRecording) {
                encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (!isEOS) {
                        break;
                    }
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    if (DEBUG) Log.v(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                    // this shoud not come when encoding
                    encoderOutputBuffers = mediaCodec.getOutputBuffers();
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (DEBUG) Log.v(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                    // this status indicate the output format of codec is changed
                    // this should come only once before actual encoded data
                    // but this status never come on Android4.3 or less
                    // and in that case, you should treat when MediaCodec.BUFFER_FLAG_CODEC_CONFIG come.
                    if (muxerStarted) {    // second time request is error
                        throw new RuntimeException("format changed twice");
                    }
                    // get output format from codec and pass them to muxer
                    // getOutputFormat should be called after INFO_OUTPUT_FORMAT_CHANGED otherwise crash.
                    final MediaFormat format = mediaCodec.getOutputFormat();
                    trackIndex = muxer.addTrack(format);
                    muxerStarted = true;
                    if (!muxer.start()) {
                        // we should wait until muxer is ready
                        synchronized (muxer) {
                            while (!muxer.isStarted())
                                try {
                                    muxer.wait(100);
                                } catch (final InterruptedException e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                } else if (encoderStatus < 0) {
                    // unexpected status
                    if (DEBUG)
                        Log.w(TAG, "drain:unexpected result from encoder#dequeueOutputBuffer: " + encoderStatus);
                } else {
                    final ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                    if (encodedData == null) {
                        // this never should come...may be a MediaCodec internal error
                        throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                    }

                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // You shoud set output format to muxer here when you target Android4.3 or less
                        // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
                        // therefor we should expand and prepare output format from buffer data.
                        // This sample is for API>=18(>=Android 4.3), just ignore this flag here
                        if (DEBUG) Log.d(TAG, "drain:BUFFER_FLAG_CODEC_CONFIG");
                        bufferInfo.size = 0;
                    }

                    if (bufferInfo.size != 0) {
                        // encoded data is ready, clear waiting counter
                        if (!muxerStarted) {
                            // muxer is not ready...this will prrograming failure.
                            throw new RuntimeException("drain:muxer hasn't started");
                        }

                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                    }

                    mediaCodec.releaseOutputBuffer(encoderStatus, false);
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isRecording = false;
                        break;
                    }
                }
            }
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    protected long getPTSUs() {
        return System.nanoTime();
    }

    public Observable<MediaEncoder> onPrepared() {
        return onPreparedSubject;
    }

    public Observable<MediaEncoder> onEnd() {
        return onEndSubject;
    }
}
