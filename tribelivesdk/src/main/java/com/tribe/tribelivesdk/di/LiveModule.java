package com.tribe.tribelivesdk.di;


import android.content.Context;

import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;

import org.java_websocket.client.DefaultSSLWebSocketClientFactory;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;

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
        SSLContext sslContext = null;

        try {
            // Will use java's default key and trust store which
            // is sufficient unless you deal with self-signed certificates
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return new WebSocketConnection(new DefaultSSLWebSocketClientFactory(sslContext), null, null);
    }

    @Provides
    @Singleton
    public WebRTCClient provideWebRTCClient(Context context) {
        return new WebRTCClient(context);
    }

    @Provides
    @Singleton
    public TribeLiveSDK provideTribeLiveSDK(WebSocketConnection webSocketConnection, WebRTCClient webRTCClient) {
        return new TribeLiveSDK(webSocketConnection, webRTCClient);
    }
}
