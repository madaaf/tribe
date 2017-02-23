package com.tribe.app.domain.entity;

import com.tribe.tribelivesdk.back.TribeLiveOptions;
import java.io.Serializable;

/**
 * Created by tiago on 30/01/2017.
 */

public class RoomConfiguration implements Serializable {

  private String room_id;
  private String websocketUrl;
  private RTCPeerConfiguration rtcPeerConfiguration;
  private @TribeLiveOptions.RoutingMode String routingMode;

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

  public @TribeLiveOptions.RoutingMode String getRoutingMode() {
    return routingMode;
  }

  public void setRoutingMode(String routingMode) {
    this.routingMode = routingMode;

    if (routingMode.equals(TribeLiveOptions.P2P)) {
      websocketUrl = "wss://coreos-3e7241e7-a2f8-43dc-84cd-93b162fa307e.tribedev.pm:48521/api";
    }
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
