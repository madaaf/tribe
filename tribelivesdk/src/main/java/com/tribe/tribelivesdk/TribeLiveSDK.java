package com.tribe.tribelivesdk;

import android.content.Context;

import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.core.Room;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 13/01/2017.
 */

@Singleton
public class TribeLiveSDK {

    private Room room;

    @Inject
    public TribeLiveSDK(WebSocketConnection webSocketConnection, WebRTCClient webRTCClient) {
        room = new Room(webSocketConnection, webRTCClient);
    }

    public Room getRoom() {
        return room;
    }
}
