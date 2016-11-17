package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import com.tribe.app.R;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@Singleton
public class SoundManager {

    public static final float SOUND_MAX = 1f;
    public static final float SOUND_MID = 0.5f;
    public static final float SOUND_LOW = 0.1f;

    public static final int TAP_TO_CANCEL = 1;
    public static final int START_RECORD = 2;
    public static final int END_RECORD = 3;
    public static final int OPEN_TRIBE = 4;
    public static final int SENT = 5;
    public static final int START_REFRESH = 6;
    public static final int END_REFRESH = 7;
    public static final int ERROR_REFRESH = 8;

    // VARIABLES
    private Context context;
    private SoundPool soundPool;
    private HashMap<Integer, Integer> soundPoolMap;
    private AudioManager audioManager;
    private Vector<Integer> availaibleSounds = new Vector<>();
    private Vector<Integer> killSoundQueue = new Vector<>();

    @Inject
    public SoundManager(Context context) {
        this.context = context;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = (new SoundPool.Builder()).setMaxStreams(20)
                    .setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            .build();
        } else {
            soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
        }

        loadSound(context);
    }
 
    public void loadSound(Context context) {
        soundPoolMap = new HashMap<Integer, Integer>();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        addSound(TAP_TO_CANCEL, R.raw.cancel);
        addSound(SENT, R.raw.sent);
        addSound(OPEN_TRIBE, R.raw.open_tribe);
        addSound(END_RECORD, R.raw.end_record);
        addSound(START_RECORD, R.raw.start_record);
        addSound(START_REFRESH, R.raw.start_refreshing);
        addSound(END_REFRESH, R.raw.end_refreshing);
        addSound(ERROR_REFRESH, R.raw.refreshing_error);
    }

    public void addSound(int index, int soundID) {
        availaibleSounds.add(index);
        soundPool.setOnLoadCompleteListener((soundPool1, sampleId, status) -> {
            //System.out.println("SAMPLE ID : " + sampleId + " / STATUS : " + status);
        });
        soundPoolMap.put(index, soundPool.load(context, soundID, 1));
    }

    public void playSound(int index, float volumeRate) {
        if (availaibleSounds.contains(index)) {
            int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float finalVol = volumeRate < streamVolume ? volumeRate : streamVolume;
            int soundId = soundPool.play(soundPoolMap.get(index), finalVol, finalVol, 1, 0, 1f);

            killSoundQueue.add(soundId);

            Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        if (!killSoundQueue.isEmpty()) {
                            Integer killSound = killSoundQueue.firstElement();
                            soundPool.stop(killSound);
                            killSoundQueue.remove(killSound);
                        }
                    });
        }
    }
}