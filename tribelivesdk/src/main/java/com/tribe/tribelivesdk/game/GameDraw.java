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
  private String previousIdChoose = null;

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
      Timber.i("SOEF nameList  empty ");
      return;
    }
    currentDrawName = nameList.get(new Random().nextInt(nameList.size()));
    Timber.i("SOEF Set current game name  : " + currentDrawName);

    if (guestList == null || guestList.size() == 0) {
      Timber.i("SOEF guestList  empty ");
      return;
    }
    currentDrawer = getRandomGuest(guestList);
    Timber.i("SOEF set current Guest : " + currentDrawer.getDisplayName());
  }

  public boolean hasNames() {
    return nameList != null && nameList.size() > 0;
  }

  private TribeGuest getRandomGuest(List<TribeGuest> array) {
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
