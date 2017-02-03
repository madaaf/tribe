package com.tribe.tribelivesdk;

import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.core.Room;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 13/01/2017.
 */

@Singleton public class TribeLiveSDK {

  private WebSocketConnection webSocketConnection;
  private WebRTCClient webRTCClient;

  @Inject public TribeLiveSDK(WebSocketConnection webSocketConnection, WebRTCClient webRTCClient) {
    this.webSocketConnection = webSocketConnection;
    this.webRTCClient = webRTCClient;
  }

  public Room newRoom() {
    return new Room(webSocketConnection, webRTCClient);
  }
}
