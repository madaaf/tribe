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

  public static final int NO_SOUND = -1;
  public static final int CALL_RING = 0;
  public static final int WAITING_FRIEND = 1;
  public static final int FRIEND_ONLINE = 2;
  public static final int JOIN_CALL = 3;
  public static final int QUIT_CALL = 4;
  public static final int WIZZ = 5;
  public static final int ALIENS_ATTACK_KILLED = 6;
  public static final int ALIENS_ATTACK_SOUNDTRACK = 7;
  public static final int GAME_SCORE = 8;
  public static final int TRIVIA_ANSWER_FIRST_WIN = 9;
  public static final int TRIVIA_LOST = 10;
  public static final int TRIVIA_SOUNDTRACK_ANSWER = 11;
  public static final int TRIVIA_SOUNDTRACK = 12;
  public static final int TRIVIA_WON = 13;
<<<<<<< HEAD
  public static final int GAME_FRIEND_LEADER = 14;
  public static final int GAME_CHALLENGING = 15;
=======
  public static final int BIRD_RUSH_SOUNDTRACK = 14;
  public static final int BIRD_RUSH_TAP = 15;
  public static final int BIRD_RUSH_OBSTACLE = 16;
  public static final int GAME_PLAYER_LOST = 17;
>>>>>>> bird_rush_scrolling

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
    addSound(ALIENS_ATTACK_KILLED, R.raw.aliens_attack_alien_killed);
    addSound(ALIENS_ATTACK_SOUNDTRACK, R.raw.aliens_attack_soundtrack);
    addSound(GAME_SCORE, R.raw.game_score);
    addSound(TRIVIA_ANSWER_FIRST_WIN, R.raw.trivia_answer_first_win);
    addSound(TRIVIA_LOST, R.raw.trivia_lost);
    addSound(TRIVIA_SOUNDTRACK_ANSWER, R.raw.trivia_soundtrack_answer);
    addSound(TRIVIA_SOUNDTRACK, R.raw.trivia_soundtrack);
    addSound(TRIVIA_WON, R.raw.trivia_won);
<<<<<<< HEAD
    addSound(GAME_FRIEND_LEADER, R.raw.game_friend_leader);
    addSound(GAME_CHALLENGING, R.raw.game_challenge);
=======
    addSound(BIRD_RUSH_SOUNDTRACK, R.raw.bird_rush_soundtrack);
    addSound(BIRD_RUSH_TAP, R.raw.bird_rush_tap);
    addSound(BIRD_RUSH_OBSTACLE, R.raw.bird_rush_obstacle_1);
    addSound(GAME_PLAYER_LOST, R.raw.game_player_lost);
>>>>>>> bird_rush_scrolling
  }

  public void addSound(int index, int soundID) {
    availaibleSounds.add(index);
    soundsRawIds.add(index, soundID);
    soundPool.setOnLoadCompleteListener((soundPool1, sampleId, status) -> {
    });
    soundPoolMap.put(index, soundPool.load(context, soundID, 1));
  }

  public void playSound(int index, float volumeRate) {
    if (index == NO_SOUND) {
      cancelMediaPlayer();
    } else if (index == WAITING_FRIEND
        || index == CALL_RING
        || index == ALIENS_ATTACK_SOUNDTRACK
        || index == BIRD_RUSH_SOUNDTRACK
        || index == TRIVIA_SOUNDTRACK
        || index == TRIVIA_SOUNDTRACK_ANSWER) {
      if (mediaPlayer != null) cancelMediaPlayer();

      mediaPlayer = MediaPlayer.create(context, soundsRawIds.get(index));
      if (mediaPlayer != null) {
        mediaPlayer.setVolume(volumeRate, volumeRate);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
      }
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

  public void setMute(boolean ismute) {
    if (ismute) {
      audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    } else {
      audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
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