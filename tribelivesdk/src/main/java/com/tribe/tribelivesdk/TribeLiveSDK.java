package com.tribe.tribelivesdk;

import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.core.Room;
import com.tribe.tribelivesdk.game.GameManager;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 13/01/2017.
 */

@Singleton public class TribeLiveSDK {

  private WebRTCClient webRTCClient;
  private GameManager gameManager;

  @Inject public TribeLiveSDK(WebRTCClient webRTCClient, GameManager gameManager) {
    this.webRTCClient = webRTCClient;
    this.gameManager = gameManager;
  }

  public Room newRoom() {
    return new Room(WebSocketConnection.newInstance(), webRTCClient);
  }

  public GameManager getGameManager() {
    return gameManager;
  }
}
