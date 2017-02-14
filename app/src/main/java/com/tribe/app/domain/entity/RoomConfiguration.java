package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 30/01/2017.
 */

public class RoomConfiguration implements Serializable {

  private String room_id;
  private String websocketUrl;
  private RTCPeerConfiguration rtcPeerConfiguration;

  public String getRoomId() {
    return room_id;
  }

  public void setRoomId(String roomId) {
    this.room_id = roomId;
  }

  public String getWebsocketUrl() {
    return websocketUrl;
  }

  public void setWebsocketUrl(String websocketUrl) {
    this.websocketUrl = websocketUrl;
  }

  public RTCPeerConfiguration getRtcPeerConfiguration() {
    return rtcPeerConfiguration;
  }

  public void setRtcPeerConfiguration(RTCPeerConfiguration rtcPeerConfiguration) {
    this.rtcPeerConfiguration = rtcPeerConfiguration;
  }

  @Override public String toString() {
    return "room_id : "
        + room_id
        + "\n"
        + "webSocketUrl : "
        + websocketUrl
        + "\n"
        + rtcPeerConfiguration
        + "\n";
  }
}
