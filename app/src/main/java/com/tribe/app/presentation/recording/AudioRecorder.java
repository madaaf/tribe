package com.tribe.app.presentation.recording;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.tribe.app.presentation.view.camera.view.VisualizerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * Created by tiago on 2016/06/13.
 */
public class AudioRecorder {

    private static final int RECORDING_SAMPLE_RATE = 44100;

    private AudioRecord audioRecord;
    private boolean isRecording;
    private int bufferSize;

    private CalculateVolumeListener volumeListener;
    private int samplingInterval = 100;
    private Timer timer;

    private List<VisualizerView> visualizerViews = new ArrayList<>();

    public AudioRecorder() {
        initAudioRecord();
    }

    public void link(VisualizerView visualizerView) {
        visualizerViews.add(visualizerView);
    }

    public void setVolumeListener(CalculateVolumeListener volumeListener) {
        this.volumeListener = volumeListener;
    }

    public void setSamplingInterval(int samplingInterval) {
        this.samplingInterval = samplingInterval;
    }

    public boolean isRecording() {
        return isRecording;
    }

    private void initAudioRecord() {
        int bufferSize = AudioRecord.getMinBufferSize(
                RECORDING_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDING_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );

        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            this.bufferSize = bufferSize;
        }
    }

    public void startRecording() {
        timer = new Timer();
        audioRecord.startRecording();
        isRecording = true;
        runRecording();
    }

    public void stopRecording() {
        isRecording = false;
        timer.cancel();

        if (visualizerViews != null && !visualizerViews.isEmpty()) {
            for (int i = 0; i < visualizerViews.size(); i++) {
                visualizerViews.get(i).receive(0);
            }
        }
    }

    private void runRecording() {
        final byte buf[] = new byte[bufferSize];
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isRecording) {
                    audioRecord.stop();
                    return;
                }
                audioRecord.read(buf, 0, bufferSize);

                int decibel = calculateDecibel(buf);
                if (visualizerViews != null && !visualizerViews.isEmpty()) {
                    for (int i = 0; i < visualizerViews.size(); i++) {
                        visualizerViews.get(i).receive(decibel);
                    }
                }

                if (volumeListener != null) {
                    volumeListener.onCalculateVolume(decibel);
                }
            }
        }, 0, samplingInterval);
    }

    private int calculateDecibel(byte[] buf) {
        int sum = 0;

        for (int i = 0; i < bufferSize; i++) {
            sum += Math.abs(buf[i]);
        }

        return sum / bufferSize;
    }

    public void release() {
        stopRecording();
        audioRecord.release();
        audioRecord = null;
        timer = null;
    }

    public interface CalculateVolumeListener {

        /**
         * Calculate input volume
         *
         * @param volume mic-input volume
         */
        void onCalculateVolume(int volume);
    }
}
