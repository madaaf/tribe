package com.tribe.tribelivesdk.back;

import android.support.annotation.Nullable;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import timber.log.Timber;

public class WebSocketConnectionOkhttp extends WebSocketConnectionAbs {

  public static WebSocketConnectionOkhttp newInstance() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS);

    SSLContext sslContext = null;

    try {
      sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, new TrustManager[] {
          new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
              Timber.d("getAcceptedIssuers =============");
              return new X509Certificate[0];
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

    builder.sslSocketFactory(sslContext.getSocketFactory()).pingInterval(5, TimeUnit.SECONDS);

    return new WebSocketConnectionOkhttp(builder.build());
  }

  private static final int NORMAL_CLOSURE_STATUS = 1000;

  private OkHttpClient client;
  private Request request;
  private WebSocket webSocket;

  // OBSERVABLE

  public WebSocketConnectionOkhttp(OkHttpClient client) {
    state = STATE_NEW;
    this.pendingMessages = new HashSet<>();

    this.client = client;
  }

  public void connect(final String url) {
    if (state == STATE_CONNECTED || state == STATE_CONNECTING) {
      Timber.d("WebSocket is already connected or connecting.");
      return;
    }

    this.url = url;
    shouldReconnect = true;
    retrying = false;
    close = false;

    Request.Builder builder = new Request.Builder().url(url);

    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        builder.addHeader(entry.getKey(), entry.getValue());
      }
    }

    request = builder.build();

    Timber.d("Connecting WebSocket to: " + url);

    webSocket = client.newWebSocket(request, new WebSocketListener() {
      @Override public void onOpen(WebSocket webSocket, Response response) {
        Timber.d("WebSocket connection opened to: " + url);
        attempts = 1;
        state = STATE_CONNECTED;
        onStateChanged.onNext(state);
        processPendingMessages();
      }

      @Override public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        if (state == STATE_CONNECTED) {
          Timber.d("On websocket text message : " + text);
          onMessage.onNext(text);
        }
      }

      @Override public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        Timber.d("onMessage : " + bytes.toString());
      }

      @Override public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        Timber.d("onClosing : " + code + " / " + reason);
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
      }

      @Override public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        Timber.d("onClosed : " + code + " / " + reason);
        synchronized (closeLock) {
          close = true;
          closeLock.notify();
        }

        if (!state.equals(STATE_DISCONNECTED)) {
          state = STATE_DISCONNECTED;
          onStateChanged.onNext(state);
        }
      }

      @Override
      public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        Timber.e("onFailure : " + t);

        onConnectError.onNext(t.getMessage());
        retry();

        if (!state.equals(STATE_ERROR)) {
          state = STATE_ERROR;
          onStateChanged.onNext(state);
        }
      }
    });

    state = WebSocketConnection.STATE_CONNECTING;
    onStateChanged.onNext(state);
  }

  public void disconnect(boolean waitForComplete) {
    Timber.d("Disconnect");

    shouldReconnect = false;
    subscriptions.clear();

    if (webSocket != null && (state == STATE_CONNECTED || state == STATE_CONNECTING)) {
      Timber.d("Disconnecting");
      state = STATE_DISCONNECTED;

      webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");

      // Wait for WebSocket close event to prevent WS library from
      // sending any pending messages to deleted looper thread.
      if (waitForComplete) {
        synchronized (closeLock) {
          while (!close) {
            try {
              closeLock.wait(1000);
              break;
            } catch (InterruptedException e) {
              Timber.d("Wait error: " + e.toString());
            }
          }
        }
      }

      webSocket = null;
    }
  }

  public void send(String msg) {
    if (webSocket == null || !isConnected()) {
      pendingMessages.add(msg);
      return;
    }

    Timber.v("Sending : " + msg);
    webSocket.send(msg);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////
}
