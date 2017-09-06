package com.tribe.app.domain.entity;

import com.tribe.tribelivesdk.back.IceConfig;
import java.io.Serializable;
import java.util.List;

/**
 * Created by tiago on 23/08/2017.
 */

public class RoomCoordinates implements Serializable {

  private String websocket_url;
  private List<IceConfig> ice_servers;

  public void setIceServers(List<IceConfig> iceServers) {
    this.ice_servers = iceServers;
  }

  public List<IceConfig> getIceServers() {
    return ice_servers;
  }

  public String getUrl() {
    return websocket_url;
  }

  @Override public String toString() {
    return "iceServers : " + ice_servers + "\n" + "websocket_url : " + websocket_url;
  }
}
