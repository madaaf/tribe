package com.tribe.app.domain.entity;

import com.tribe.tribelivesdk.back.IceConfig;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    // TODO remove when working in api
    if (routingMode.equals(TribeLiveOptions.ROUTED)) {
      websocketUrl = "wss://coreos-3e7241e7-a2f8-43dc-84cd-93b162fa307e.tribedev.pm:43567/api";
      List<IceConfig> iceServerList = new ArrayList<>();
      List<String> urls = new ArrayList<>();
      urls.add("turn:coreos-3e7241e7-a2f8-43dc-84cd-93b162fa307e.tribedev.pm:49921");
      iceServerList.add(new IceConfig(urls, "gorst", "hero"));
      rtcPeerConfiguration.setIceServers(iceServerList);
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
