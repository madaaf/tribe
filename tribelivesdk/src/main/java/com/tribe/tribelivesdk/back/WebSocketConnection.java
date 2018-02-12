package com.tribe.tribelivesdk.back;

import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import timber.log.Timber;

public class WebSocketConnection extends WebSocketConnectionAbs {

  public static WebSocketConnection newInstance() {
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
    return new WebSocketConnection(factory);
  }

  private WebSocket webSocketClient;
  private WebSocketFactory clientFactory;

  // OBSERVABLE

  public WebSocketConnection(WebSocketFactory clientFactory) {
    state = STATE_NEW;
    this.clientFactory = clientFactory;
    this.pendingMessages = new HashSet<>();
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

    URI uri;
    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return;
    }

    Timber.d("Connecting WebSocket to: " + url);

    try {
      webSocketClient = clientFactory.createSocket(uri, CONNECT_TIMEOUT);

      if (headers != null) {
        if (headers.containsKey(PROTOCOL)) webSocketClient.addProtocol(headers.get(PROTOCOL));

        for (Map.Entry<String, String> entry : headers.entrySet()) {
          if (!entry.getKey().equals(PROTOCOL) && !entry.getKey().equals(VERSION)) {
            webSocketClient.addHeader(entry.getKey(), entry.getValue());
          }
        }
      }

      webSocketClient.addListener(new WebSocketListener() {

        @Override public void onStateChanged(WebSocket websocket,
            com.neovisionaries.ws.client.WebSocketState newState) throws Exception {
          Timber.d("WebSocket stateChanged: " + newState.name());
        }

        @Override public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
            throws Exception {
          Timber.d("WebSocket connection opened to: " + url);
          attempts = 1;
          state = STATE_CONNECTED;
          onStateChanged.onNext(state);
          processPendingMessages();
        }

        @Override public void onConnectError(WebSocket websocket, WebSocketException cause)
            throws Exception {
          Timber.d("WebSocket onConnectError: " + cause.getMessage());
          onConnectError.onNext(cause.getMessage());
          retry();

          if (!state.equals(STATE_ERROR)) {
            state = STATE_ERROR;
            onStateChanged.onNext(state);
          }
        }

        @Override public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
            WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
          Timber.d("WebSocket onDisconnected");

          synchronized (closeLock) {
            close = true;
            closeLock.notify();
          }

          if (!state.equals(STATE_DISCONNECTED)) {
            state = STATE_DISCONNECTED;
            onStateChanged.onNext(state);
          }

          retry();
        }

        @Override public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame)
            throws Exception {

        }

        @Override public void onTextFrame(WebSocket websocket, WebSocketFrame frame)
            throws Exception {

        }

        @Override public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame)
            throws Exception {

        }

        @Override public void onCloseFrame(WebSocket websocket, WebSocketFrame frame)
            throws Exception {
          Timber.d("WebSocket onCloseFrame");
        }

        @Override public void onPingFrame(WebSocket websocket, WebSocketFrame frame)
            throws Exception {
          webSocketClient.sendPong("{}".getBytes());
        }

        @Override public void onPongFrame(WebSocket websocket, WebSocketFrame frame)
            throws Exception {
        }

        @Override public void onTextMessage(WebSocket websocket, String text) throws Exception {
          if (state == STATE_CONNECTED) {
            Timber.d("On websocket text message : " + text);
            onMessage.onNext(text);
          }
        }

        @Override public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
          Timber.d("WebSocket onBinaryMessage");
        }

        @Override public void onSendingFrame(WebSocket websocket, WebSocketFrame frame)
            throws Exception {

        }

        @Override public void onFrameSent(WebSocket websocket, WebSocketFrame frame)
            throws Exception {

        }

        @Override public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame)
            throws Exception {

        }

        @Override public void onError(WebSocket websocket, WebSocketException cause)
            throws Exception {
          Timber.d("WebSocket onError : " + cause.getError().name());

          state = STATE_ERROR;
          onError.onNext(cause.getMessage());
        }

        @Override public void onFrameError(WebSocket websocket, WebSocketException cause,
            WebSocketFrame frame) throws Exception {

        }

        @Override public void onMessageError(WebSocket websocket, WebSocketException cause,
            List<WebSocketFrame> frames) throws Exception {
          Timber.d("WebSocket onMessageError : " + cause.getMessage());
        }

        @Override
        public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause,
            byte[] compressed) throws Exception {

        }

        @Override
        public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data)
            throws Exception {
          Timber.d("WebSocket onTextMessageMessageError : " + cause.getMessage());
        }

        @Override
        public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame)
            throws Exception {
          Timber.d("WebSocket onSendError : " + cause.getMessage());
        }

        @Override public void onUnexpectedError(WebSocket websocket, WebSocketException cause)
            throws Exception {
          Timber.d("WebSocket onUnexpectedError : " + cause);
        }

        @Override public void handleCallbackError(WebSocket websocket, Throwable cause)
            throws Exception {
          cause.printStackTrace();
          Timber.d("WebSocket handleCallbackError : " + cause.getMessage());
          disconnect(false);
        }

        @Override public void onSendingHandshake(WebSocket websocket, String requestLine,
            List<String[]> headers) throws Exception {
          Timber.d("WebSocket onSendingHandshake : " + requestLine);
        }

        @Override
        public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread)
            throws Exception {

        }

        @Override
        public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread)
            throws Exception {

        }

        @Override
        public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread)
            throws Exception {

        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }

    state = WebSocketConnection.STATE_CONNECTING;
    onStateChanged.onNext(state);

    webSocketClient.connectAsynchronously();
    webSocketClient.setAutoFlush(true);
    webSocketClient.setPingInterval(5 * 1000); // 60 SECONDS
    webSocketClient.setPingPayloadGenerator(() -> "{}".getBytes());
  }

  public void disconnect(boolean waitForComplete) {
    Timber.d("Disconnect");

    shouldReconnect = false;
    subscriptions.clear();

    if (webSocketClient != null && (state == STATE_CONNECTED || state == STATE_CONNECTING)) {
      Timber.d("Disconnecting");
      state = STATE_DISCONNECTED;
      webSocketClient.setPongInterval(0);
      webSocketClient.setPingInterval(0);
      webSocketClient.disconnect();

      // Wait for WebSocket close event to prevent WS library from
      // sending any pending messages to deleted looper thread.
      if (waitForComplete) {
        synchronized (closeLock) {
          while (!close) {
            try {
              closeLock.wait(CLOSE_TIMEOUT);
              break;
            } catch (InterruptedException e) {
              Timber.d("Wait error: " + e.toString());
            }
          }
        }
      }

      webSocketClient.clearListeners();
      webSocketClient.clearUserInfo();
      webSocketClient = null;
    }
  }

  public void send(String msg) {
    if (webSocketClient == null || !isConnected()) {
      pendingMessages.add(msg);
      return;
    }

    Timber.v("Sending : " + msg);
    webSocketClient.sendText(msg);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////
}
