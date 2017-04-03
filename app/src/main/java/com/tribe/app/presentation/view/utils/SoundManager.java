package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@Singleton public class SoundManager {

  public static final float SOUND_MAX = 1f;
  public static final float SOUND_MID = 0.5f;
  public static final float SOUND_LOW = 0.1f;

  public static final int CALL_RING = 0;
  public static final int WAITING_FRIEND = 1;
  public static final int FRIEND_ONLINE = 2;
  public static final int JOIN_CALL = 3;
  public static final int QUIT_CALL = 4;
  public static final int WIZZ = 5;

  // VARIABLES
  private Context context;
  private Preference<Boolean> uiSounds;
  private SoundPool soundPool;
  private HashMap<Integer, Integer> soundPoolMap;
  private AudioManager audioManager;
  private Vector<Integer> availaibleSounds = new Vector<>();
  private Vector<Integer> soundsRawIds = new Vector<>();
  private Vector<Integer> killSoundQueue = new Vector<>();
  private MediaPlayer mediaPlayer;

  @Inject public SoundManager(Context context, Preference<Boolean> uiSounds) {
    this.context = context;
    this.uiSounds = uiSounds;

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      soundPool = (new SoundPool.Builder()).setMaxStreams(20)
          .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build())
          .build();
    } else {
      soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
    }

    loadSound(context);
  }

  public void loadSound(Context context) {
    soundPoolMap = new HashMap<>();
    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    addSound(CALL_RING, R.raw.call_ring);
    addSound(WAITING_FRIEND, R.raw.waiting_friend);
    addSound(FRIEND_ONLINE, R.raw.friend_online);
    addSound(JOIN_CALL, R.raw.join_call);
    addSound(QUIT_CALL, R.raw.quit_call);
    addSound(WIZZ, R.raw.wizz);
  }

  public void addSound(int index, int soundID) {
    availaibleSounds.add(index);
    soundsRawIds.add(index, soundID);
    soundPool.setOnLoadCompleteListener((soundPool1, sampleId, status) -> {
    });
    soundPoolMap.put(index, soundPool.load(context, soundID, 1));
  }

  public void playSound(int index, float volumeRate) {
    if (index == WAITING_FRIEND || index == CALL_RING) {
      if (mediaPlayer != null) cancelMediaPlayer();

      mediaPlayer = MediaPlayer.create(context, soundsRawIds.get(index));
      mediaPlayer.setVolume(volumeRate, volumeRate);
      mediaPlayer.setLooping(true);
      mediaPlayer.start();
    } else {
      if (availaibleSounds.contains(index) && uiSounds.get()) {
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float finalVol = volumeRate < streamVolume ? volumeRate : streamVolume;
        int soundId = soundPool.play(soundPoolMap.get(index), finalVol, finalVol, 1, 0, 1f);

        killSoundQueue.add(soundId);

        Observable.timer(5000, TimeUnit.MILLISECONDS)
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

  public void cancelMediaPlayer() {
    if (mediaPlayer != null) {
      mediaPlayer.reset();
      mediaPlayer.stop();
      mediaPlayer.release();
      mediaPlayer = null;
    }
  }
}