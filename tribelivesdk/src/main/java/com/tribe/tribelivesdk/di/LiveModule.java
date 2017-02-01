package com.tribe.tribelivesdk.di;


import android.content.Context;

import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;

import com.tribe.tribelivesdk.util.LogUtil;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        LogUtil.d(getClass(), "getAcceptedIssuers =============");
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        LogUtil.d(getClass(), "checkClientTrusted =============");
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        LogUtil.d(getClass(), "checkServerTrusted =============");
                    }
                }
            }, new SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
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
