package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import timber.log.Timber;

/**
 * Created by tiago on 23/05/2017.
 */

public class GameChallenge extends Game {

  private List<String> nameList;
  private List<TribeGuest> guestList;

  private String currentChallenge;
  private TribeGuest currentChallenger;

  public GameChallenge(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
    nameList = new ArrayList<>();
    guestList = new ArrayList<>();
  }

  public void setNewDatas(List<String> nameList, List<TribeGuest> peopleInRoom) {
    this.nameList.clear();
    this.guestList.clear();
    this.nameList.addAll(nameList);
    this.guestList.addAll(peopleInRoom);
    generateNewDatas();
  }

  public void setGuestList(List<TribeGuest> guestList) {
    this.guestList = guestList;
  }

  public void generateNewDatas() {
    if (nameList == null || nameList.size() == 0) {
      Timber.i("SOEF nameList  empty ");
      return;
    }
    currentChallenge = nameList.get(new Random().nextInt(nameList.size()));
    Timber.i("SOEF Set current game name  : " + currentChallenge);

    if (guestList == null || guestList.size() == 0) {
      Timber.i("SOEF guestList  empty ");
      return;
    }
    currentChallenger = getRandomGuest(guestList);
    Timber.i("SOEF set current Guest : " + currentChallenger.getDisplayName());
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

  public boolean hasNames() {
    return nameList != null && nameList.size() > 0;
  }

  private static TribeGuest getRandomGuest(List<TribeGuest> array) {
    int rnd = new Random().nextInt(array.size());
    return array.get(rnd);
  }

}
