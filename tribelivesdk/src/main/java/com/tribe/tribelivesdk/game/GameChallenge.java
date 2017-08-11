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
  private String previousGuestId = null;

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
      return;
    }
    currentChallenge = nameList.get(new Random().nextInt(nameList.size()));
    Timber.d("set current game name  : " + currentChallenge);

    if (guestList == null || guestList.size() == 0) {
      return;
    }

    currentChallenger = getNextGamer(guestList);
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

  public boolean hasNames() {
    return nameList != null && nameList.size() > 0;
  }

  private TribeGuest getNextGamer(List<TribeGuest> array) {
    Collections.sort(array, (o1, o2) -> o1.getId().compareTo(o2.getId()));
    TribeGuest tribeGuest;

    if (previousGuestId == null) {
      tribeGuest = array.get(new Random().nextInt(array.size()));
      previousGuestId = tribeGuest.getId();
      return tribeGuest;
    } else {
      int index = 0;
      for (int i = 0; i < array.size(); i++) {
        if (array.get(i).getId().equals(previousGuestId)) {
          index = i + 1;
          break;
        }
      }

      if (index >= array.size()) index = 0;

      tribeGuest = array.get(index);
      previousGuestId = tribeGuest.getId();
      return tribeGuest;
    }
  }
}
