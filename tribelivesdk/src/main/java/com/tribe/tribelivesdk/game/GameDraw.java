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
 * Created by madaaflak on 31/07/2017.
 */

public class GameDraw extends Game {
  private List<String> nameList;
  private List<TribeGuest> guestList;

  private String currentDrawName = "";
  private TribeGuest currentDrawer = null;
  private String previousGuestId = null;

  public GameDraw(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
    nameList = new ArrayList<>();
    guestList = new ArrayList<>();
  }

  @Override public void apply(Frame frame) {

  }

  @Override public void onFrameSizeChange(Frame frame) {

  }

  public void setGuestList(List<TribeGuest> guestList) {
    this.guestList = guestList;
  }

  public void setNewDatas(List<String> nameList, List<TribeGuest> peopleInRoom) {
    this.nameList.clear();
    this.guestList.clear();
    this.nameList.addAll(nameList);
    this.guestList.addAll(peopleInRoom);
    generateNewDatas();
  }

  public String getCurrentDrawName() {
    return currentDrawName;
  }

  public TribeGuest getCurrentDrawer() {
    return currentDrawer;
  }

  public void setCurrentDrawer(TribeGuest currentDrawer) {
    this.currentDrawer = currentDrawer;
  }

  public void setCurrentDrawName(String currentDrawName) {
    this.currentDrawName = currentDrawName;
  }

  public void generateNewDatas() {
    if (nameList == null || nameList.size() == 0) {
      return;
    }
    currentDrawName = nameList.get(new Random().nextInt(nameList.size()));
    Timber.d("set current game name  : " + currentDrawName);

    if (guestList == null || guestList.size() == 0) {
      return;
    }
    currentDrawer = getNextGuest(guestList);
    Timber.d("set current gamer : " + currentDrawer.getDisplayName());
  }

  public boolean hasNames() {
    return nameList != null && nameList.size() > 0;
  }

  private TribeGuest getNextGuest(List<TribeGuest> array) {
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
