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

    private Context context;
    private TribeLiveOptions options;
    private WebSocketConnection webSocketConnection;
    private WebRTCClient webRTCClient;
    private Room room;

    @Inject
    public TribeLiveSDK(Context context, WebSocketConnection webSocketConnection, WebRTCClient webRTCClient, TribeLiveOptions options) {
        this.context = context;
        this.options = options;
        this.webSocketConnection = webSocketConnection;
        this.webRTCClient = webRTCClient;

        room = new Room(webSocketConnection, webRTCClient, options);
    }

    public Room getRoom() {
        return room;
    }
}
