package com.tribe.tribelivesdk.di;

import android.content.Context;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.WebRTCClient;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import dagger.Module;
import dagger.Provides;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import timber.log.Timber;

/**
 * Created by tiago on 01/13/17.
 */

@Module public class LiveModule {

  @Provides @Singleton public WebSocketConnection provideWebSocketConnection() {
    SSLContext sslContext = null;

    try {
      sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, new TrustManager[] {
          new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
              Timber.d("getAcceptedIssuers =============");
              return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
              Timber.d("checkClientTrusted =============");
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
              Timber.d("checkServerTrusted =============");
            }
          }
      }, new SecureRandom());
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    WebSocketFactory factory = new WebSocketFactory();
    factory.setSSLContext(sslContext);
    return new WebSocketConnection(factory, null);
  }

  @Provides @Singleton public WebRTCClient provideWebRTCClient(Context context) {
    return new WebRTCClient(context);
  }

  @Provides @Singleton
  public TribeLiveSDK provideTribeLiveSDK(WebSocketConnection webSocketConnection,
      WebRTCClient webRTCClient) {
    return new TribeLiveSDK(webSocketConnection, webRTCClient);
  }
}
