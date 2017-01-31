package com.tribe.app.domain.entity;

import com.tribe.tribelivesdk.back.IceConfig;
import java.util.List;

/**
 * Created by tiago on 30/01/2017.
 */

public class RTCPeerConfiguration {

  private List<IceConfig> iceServers;

  public void setIceServers(List<IceConfig> iceServers) {
    this.iceServers = iceServers;
  }

  public List<IceConfig> getIceServers() {
    return iceServers;
  }

  @Override public String toString() {
    return "iceServers : " + iceServers + "\n";
  }
}
