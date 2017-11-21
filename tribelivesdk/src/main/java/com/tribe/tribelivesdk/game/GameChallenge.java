package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.Random;
import timber.log.Timber;

/**
 * Created by tiago on 23/05/2017.
 */

public class GameChallenge extends Game {

  private String currentChallenge;
  private TribeGuest currentChallenger;

  public GameChallenge(Context context, String id) {
    super(context, id);
  }

  public void generateNewDatas() {
    if (dataList == null || dataList.size() == 0) {
      return;
    }
    currentChallenge = dataList.get(new Random().nextInt(dataList.size()));
    Timber.d("set current game name  : " + currentChallenge);

    if (dataList == null || dataList.size() == 0) {
      return;
    }

    currentChallenger = getNextGuest();
    Timber.d("set current gamer : " + currentChallenger.getDisplayName());
  }

  @Override public void apply(Frame frame) {
  }

  @Override public void onFrameSizeChange(Frame frame) {
  }

  public String getCurrentChallenge() {
    return currentChallenge;
  }

  public void setCurrentChallenge(String currentChallenge) {
    this.currentChallenge = currentChallenge;
  }

  public TribeGuest getCurrentChallenger() {
    return currentChallenger;
  }

  public void setCurrentChallenger(TribeGuest currentChallenger) {
    this.currentChallenger = currentChallenger;
  }
}
