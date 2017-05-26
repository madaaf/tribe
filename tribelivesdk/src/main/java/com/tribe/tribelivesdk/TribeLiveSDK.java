package com.tribe.tribelivesdk;

import android.content.Context;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.core.Room;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.ulsee.UlseeManager;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 13/01/2017.
 */

@Singleton public class TribeLiveSDK {

  private WebRTCClient webRTCClient;

  @Inject public TribeLiveSDK(Context context, WebRTCClient webRTCClient) {
    this.webRTCClient = webRTCClient;
  }

  public Room newRoom() {
    return new Room(WebSocketConnection.newInstance(), webRTCClient);
  }
}
