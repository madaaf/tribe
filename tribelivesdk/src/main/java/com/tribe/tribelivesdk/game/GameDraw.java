package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.ArrayList;
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
  private TribeGuest currentGuest = null;
  private boolean myTurn = true;

  public GameDraw(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
    nameList = new ArrayList<>();
    guestList = new ArrayList<>();
  }

  @Override public void apply(Frame frame) {

  }

  @Override public void onFrameSizeChange(Frame frame) {

  }

  public void setNewDatas(List<String> nameList, List<TribeGuest> peopleInRoom) {
    this.nameList.clear();
    this.guestList.clear();
    this.nameList.addAll(nameList);
    this.guestList.addAll(peopleInRoom);
    generateNewDatas();
  }

  public boolean isMyTurn() {
    return myTurn;
  }

  public void setMyTurn(boolean myTurn) {
    this.myTurn = myTurn;
  }

  public String getCurrentDrawName() {
    return currentDrawName;
  }

  public TribeGuest getCurrentGuest() {
    return currentGuest;
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
    currentGuest = getRandomGuest(guestList);
    Timber.i("SOEF set current Guest : " + currentGuest.getDisplayName());
  }

  public boolean hasNames() {
    return nameList != null && nameList.size() > 0;
  }

  private static TribeGuest getRandomGuest(List<TribeGuest> array) {
    int rnd = new Random().nextInt(array.size());
    return array.get(rnd);
  }
}
