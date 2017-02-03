package com.tribe.tribelivesdk.back;

import android.support.annotation.StringDef;
import com.tribe.tribelivesdk.util.LogUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import rx.Observable;
import rx.subjects.PublishSubject;

@Singleton public class WebSocketConnection {

  private static final int CONNECT_TIMEOUT = 1000;
  private static final int CLOSE_TIMEOUT = 1000;

  @StringDef({ STATE_NEW, STATE_CONNECTING, STATE_CONNECTED, STATE_DISCONNECTED, STATE_ERROR })
  public @interface WebSocketState {
  }

  public static final String STATE_NEW = "new";
  public static final String STATE_CONNECTING = "connecting";
  public static final String STATE_CONNECTED = "connected";
  public static final String STATE_DISCONNECTED = "disconnected";
  public static final String STATE_ERROR = "error";

  private @WebSocketState String state;
  private WebSocketClient webSocketClient;
  private WebSocketClient.WebSocketClientFactory clientFactory;
  private Draft draft;
  private Map<String, String> headers;
  private final Object closeLock = new Object();
  private boolean close;

  private PublishSubject<String> onStateChanged = PublishSubject.create();
  private PublishSubject<String> onMessage = PublishSubject.create();
  private PublishSubject<String> onError = PublishSubject.create();

  @Inject
  public WebSocketConnection(WebSocketClient.WebSocketClientFactory clientFactory, Draft draft,
      Map<String, String> headers) {
    state = STATE_NEW;
    this.draft = draft;
    this.clientFactory = clientFactory;
    this.headers = headers;
  }

  public void connect(final String url) {
    if (state == STATE_CONNECTED) {
      LogUtil.e(getClass(), "WebSocket is already connected.");
      return;
    }

    close = false;

    URI uri;
    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return;
    }

    LogUtil.d(getClass(), "Connecting WebSocket to: " + url);

    if (draft == null) draft = new Draft_17();

    webSocketClient = new WebSocketClient(uri, draft, this.headers, CONNECT_TIMEOUT) {
      @Override public void onOpen(ServerHandshake serverHandshake) {
        LogUtil.d(getClass(), "WebSocket connection opened to: " + url);
        state = STATE_CONNECTED;
        onStateChanged.onNext(state);
      }

      @Override public void onMessage(String s) {
        if (state == STATE_CONNECTED) {
          onMessage.onNext(s);
        }
      }

      @Override public void onClose(int i, String s, boolean b) {
        LogUtil.d(getClass(), "Closed " + s + " state : " + state);

        synchronized (closeLock) {
          close = true;
          closeLock.notify();
        }

        if (state != STATE_DISCONNECTED) {
          state = STATE_DISCONNECTED;
          onStateChanged.onNext(state);
        }
      }

      @Override public void onError(Exception e) {
        LogUtil.e(getClass(), "Error " + e.getMessage());

        state = STATE_ERROR;
        onError.onNext(e.getMessage());
      }

      @Override public void onClosing(int code, String reason, boolean remote) {
        LogUtil.d(getClass(), "Code : " + code + ", Reason : " + reason + ", Remote : " + remote);
      }
    };

    webSocketClient.setWebSocketFactory(clientFactory);
    webSocketClient.connect();
  }

  private boolean isConnected() {
    return state == STATE_CONNECTED;
  }

  public void disconnect(boolean waitForComplete) {
    LogUtil.d(getClass(), "Disconnect");

    if (webSocketClient != null && (state == STATE_CONNECTED || state == STATE_CONNECTING)) {
      state = STATE_DISCONNECTED;
      webSocketClient.close();

      // Wait for WebSocket close event to prevent WS library from
      // sending any pending messages to deleted looper thread.
      if (waitForComplete) {
        synchronized (closeLock) {
          while (!close) {
            try {
              closeLock.wait(CLOSE_TIMEOUT);
              break;
            } catch (InterruptedException e) {
              LogUtil.e(getClass(), "Wait error: " + e.toString());
            }
          }
        }
      }

      webSocketClient = null;
    }
  }

  public void send(String msg) {
    if (webSocketClient == null || !isConnected()) {
            /* TODO A mechanism of message queue to pile up before connecting ? */
      return;
    }

    LogUtil.v(getClass(), "Sending : " + msg);
    webSocketClient.send(msg);
  }

  public @WebSocketState String getState() {
    return state;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<String> onStateChanged() {
    return onStateChanged;
  }

  public Observable<String> onMessage() {
    return onMessage;
  }

  public Observable<String> onError() {
    return onError;
  }
}
