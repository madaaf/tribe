package com.tribe.tribelivesdk;

import android.content.Context;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.core.WebRTCRoom;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 13/01/2017.
 */

@Singleton public class TribeLiveSDK {

  private WebRTCClient webRTCClient;
  private Context context;

  @Inject public TribeLiveSDK(Context context, WebRTCClient webRTCClient) {
    this.context = context;
    this.webRTCClient = webRTCClient;
  }

  public WebRTCRoom newRoom() {
    return new WebRTCRoom(context, WebSocketConnection.newInstance(), webRTCClient);
  }
}
