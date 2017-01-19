package com.tribe.tribelivesdk.di;


import android.content.Context;

import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tiago on 01/13/17.
 */

@Module
public class LiveModule {

    @Provides
    @Singleton
    public WebSocketConnection provideWebSocketConnection() {
        return new WebSocketConnection();
    }

    @Provides
    @Singleton
    public WebRTCClient provideWebRTCClient(Context context, TribeLiveOptions options) {
        return new WebRTCClient(context, options);
    }

    @Provides
    @Singleton
    public TribeLiveSDK provideTribeLiveSDK(Context context, WebSocketConnection webSocketConnection, WebRTCClient webRTCClient, TribeLiveOptions tribeLiveOptions) {
        return new TribeLiveSDK(context, webSocketConnection, webRTCClient, tribeLiveOptions);
    }
}
