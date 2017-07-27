package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 23/05/2017.
 */

public class GameChallenge extends Game {

  private List<String> nameList;
  private List<TribeGuest> guestList;
  private String currentPostItName = "";
  private String peerId;
  private String currentChallenge;


  public GameChallenge(Context context, @GameType String id, String name, int drawableRes) {
    super(context, id, name, drawableRes);
    nameList = new ArrayList<>();
    guestList = new ArrayList<>();
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

  public String getCurrentChallengerId() {
    return peerId;
  }

  public void setPeerId(String peerId) {
    this.peerId = peerId;
  }

  public void setNameList(List<String> nameList) {
    this.nameList.addAll(nameList);
  }

  public void setGuestList(List<TribeGuest> guestList) {
    this.guestList = guestList;
  }

  public List<TribeGuest> getGuestList() {
    return guestList;
  }

  public boolean hasNames() {
    return nameList != null && nameList.size() > 0;
  }

  public List<String> getNameList() {
    return nameList;
  }
}
