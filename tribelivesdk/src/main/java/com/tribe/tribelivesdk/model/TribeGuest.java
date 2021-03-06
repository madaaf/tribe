package com.tribe.tribelivesdk.model;

import com.tribe.tribelivesdk.game.GameManager;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by tiago on 07/02/2017.
 */

public class TribeGuest extends GroupJoiner implements Serializable {

  public static final String ID = "id";
  public static final String DISPLAY_NAME = "display_name";
  public static final String PICTURE = "picture";
  public static final String USERNAME = "username";
  public static final String TROPHY = "trophy";

  private String id;
  private String displayName;
  private String userName;
  private String picture;
  private String trophy;
  private boolean isInvite;
  private List<String> memberPics;
  private boolean canRemove;
  private boolean isAnonymous = false;
  private boolean isExternal = false;
  private boolean isFriend = true;
  private int scoreValue, rankingValue;

  public TribeGuest(String id) {
    this.id = id;
  }

  public TribeGuest(String id, String displayName, String picture, boolean isInvite,
      boolean canRemove, String userName, String trophy) {
    this.id = id;
    this.displayName = displayName;
    this.picture = picture;
    this.isInvite = isInvite;
    this.canRemove = canRemove;
    this.userName = userName;
    this.trophy = trophy;
  }

  public boolean isAnonymous() {
    return isAnonymous;
  }

  public void setAnonymous(boolean anonymous) {
    isAnonymous = anonymous;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public String getId() {
    return id;
  }

  public void setMemberPics(List<String> memberPics) {
    this.memberPics = memberPics;
  }

  public List<String> getMemberPics() {
    return memberPics;
  }

  public boolean isInvite() {
    return isInvite;
  }

  public void setInvite(boolean invite) {
    isInvite = invite;
  }

  public boolean canRemove() {
    return canRemove;
  }

  public boolean isFriend() {
    return isFriend;
  }

  public void setFriend(boolean friend) {
    isFriend = friend;
  }

  public void setCanRemove(boolean canRemove) {
    this.canRemove = canRemove;
  }

  public void setExternal(boolean external) {
    isExternal = external;
  }

  public boolean isExternal() {
    return isExternal;
  }

  public boolean canPlayGames(String gameId) {
    return Arrays.asList(GameManager.playableGames).contains(gameId);
  }

  public void setScoreValue(int scoreValue) {
    this.scoreValue = scoreValue;
  }

  public int getScoreValue() {
    return scoreValue;
  }

  public void setRankingValue(int rankingValue) {
    this.rankingValue = rankingValue;
  }

  public int getRankingValue() {
    return rankingValue;
  }

  public String getTrophy() {
    return trophy;
  }

  public void setTrophy(String trophy) {
    this.trophy = trophy;
  }

  public Hashtable<Object, Object> asCoronaUser() {
    Hashtable<Object, Object> data = new Hashtable<>();
    data.put("id", id);
    data.put("displayName", displayName);
    if (trophy != null && !trophy.equals("")) data.put("trophy", trophy);
    if (picture != null && !picture.equals("")) data.put("picture", picture);
    if (userName != null && !userName.equals("")) data.put("username", userName);
    return data;
  }

  @Override public String toString() {
    return "TribeGuest{" +
        "id='" +
        id +
        '\'' +
        ", displayName='" +
        displayName +
        '\'' +
        ", userName='" +
        userName +
        '\'' +
        '}';
  }
}
