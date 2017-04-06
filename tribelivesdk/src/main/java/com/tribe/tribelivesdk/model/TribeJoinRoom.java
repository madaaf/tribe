package com.tribe.tribelivesdk.model;

import java.util.List;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribeJoinRoom {

  private int roomSize = 0;
  private List<TribeSession> sessionList;

  public TribeJoinRoom(List<TribeSession> sessionList, int roomSize) {
    this.sessionList = sessionList;
    this.roomSize = roomSize;
  }

  public TribeJoinRoom() {}

  public List<TribeSession> getSessionList() {
    return sessionList;
  }

  public int getRoomSize() {
    return roomSize;
  }
}
