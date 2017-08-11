package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.ArrayList;
import java.util.Collections;
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
  private String previousIdChoose = null;

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

    currentChallenger = getSortedGuest(guestList);
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

  private TribeGuest getSortedGuest(List<TribeGuest> array) {
    TribeGuest guestChoose = null;
    if (previousIdChoose == null) {
      int rnd = new Random().nextInt(array.size());
      guestChoose = array.get(rnd);
      previousIdChoose = guestChoose.getId();
      return guestChoose;
    }

    List<String> ids = new ArrayList<>();
    for (TribeGuest guest : array) {
      ids.add(guest.getId());
    }
    Collections.sort(ids);
    int i = ids.indexOf(previousIdChoose) + 1;
    if (i >= array.size()) i = 0;
    guestChoose = array.get(i);
    previousIdChoose = guestChoose.getId();
    return guestChoose;
  }
}
