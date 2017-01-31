package com.tribe.app.presentation.recording;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.tribe.app.presentation.view.camera.interfaces.AudioVisualizerCallback;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tiago on 2016/06/13.
 */
public class AudioRecorder {

    private static String TAG = "AudioRecorder";

    private static final int BLOCK_SIZE = 256;
    private static int[] sampleRates = new int[]{44100, 8000, 11025, 22050};
    public static final int SAMPLING_INTERVAL = 1;

    private AudioRecord audioRecord;
    private boolean isRecording;
    private int bufferSize;
    private RealDoubleFFT transformer;

    private Timer timer;

    private AudioVisualizerCallback visualizerView = null;

    public AudioRecorder() {
    }

    public void link(AudioVisualizerCallback visualizerView) {
        this.visualizerView = visualizerView;
    }

    public boolean isRecording() {
        return isRecording;
    }

    private void initAudioRecord() {
        for (int rate : sampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {
                        bufferSize = AudioRecord.getMinBufferSize(
                                rate,
                                channelConfig,
                                audioFormat
                        );

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED)
                                return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception, keep trying : " + e.getMessage());
                    }
                }
            }
        }
    }

    public void startRecording() {
        timer = new Timer();
        initAudioRecord();
        audioRecord.startRecording();
        isRecording = true;
        runRecording();
    }

    public void stopRecording() {
        isRecording = false;
        timer.cancel();
        audioRecord.release();
    }

    private void runRecording() {
        short[] buffer = new short[BLOCK_SIZE];
        double[] toTransform = new double[BLOCK_SIZE];
        transformer = new RealDoubleFFT(BLOCK_SIZE);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isRecording) {
                    audioRecord.stop();
                    return;
                }

                int bufferReadResult = audioRecord.read(buffer, 0, BLOCK_SIZE);

                for (int i = 0; i < BLOCK_SIZE && i < bufferReadResult; i++) {
                    toTransform[i] = (double) buffer[i] / 32768.0;
                }

                transformer.ft(toTransform);

                if (visualizerView != null) {
                    visualizerView.receive(toTransform);
                }
            }
        }, 100, SAMPLING_INTERVAL);
    }

    public void release() {
        stopRecording();
        audioRecord.release();
        audioRecord = null;
        timer = null;
    }
}
