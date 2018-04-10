package com.tribe.tribelivesdk.back;

import android.support.annotation.StringDef;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public abstract class WebSocketConnectionAbs {

  protected static final int CONNECT_TIMEOUT = 10000;
  protected static final int CLOSE_TIMEOUT = 10000;

  @StringDef({ STATE_NEW, STATE_CONNECTING, STATE_CONNECTED, STATE_DISCONNECTED, STATE_ERROR })
  public @interface WebSocketState {
  }

  public static final String STATE_NEW = "new";
  public static final String STATE_CONNECTING = "connecting";
  public static final String STATE_CONNECTED = "connected";
  public static final String STATE_DISCONNECTED = "disconnected";
  public static final String STATE_ERROR = "error";

  public static final String PROTOCOL = "Sec-WebSocket-Protocol";
  public static final String VERSION = "Sec-WebSocket-Version";
  public static final String AUTHORIZATION = "Authorization";
  public static final String USER_AGENT = "User-Agent";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String ORIGIN = "Origin";
  public static final String TRIBE_SESSION_ID = "trb-sess-id";

  protected @WebSocketState String state;
  protected Map<String, String> headers;
  protected Set<String> pendingMessages;
  protected final Object closeLock = new Object();
  protected boolean close, shouldReconnect = true, retrying = false;
  protected int attempts = 1;
  protected String url;

  // OBSERVABLE
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected PublishSubject<String> onStateChanged = PublishSubject.create();
  protected PublishSubject<String> onMessage = PublishSubject.create();
  protected PublishSubject<String> onConnectError = PublishSubject.create();
  protected PublishSubject<String> onError = PublishSubject.create();

  protected void processPendingMessages() {
    if (pendingMessages == null || pendingMessages.size() == 0) {
      return;
    } else {
      for (String request : pendingMessages) {
        send(request);
      }

      pendingMessages.clear();
    }
  }

  protected void retry() {
    if (shouldReconnect && !retrying) {
      retrying = true;
      int time = generateInterval(attempts);
      Timber.d("Trying to reconnect in : " + time);

      subscriptions.add(
          Observable.timer(time, TimeUnit.MILLISECONDS).onBackpressureDrop().subscribe(aLong -> {
            Timber.d("Reconnecting");
            attempts++;
            connect(url);
          }));
    }
  }

  protected boolean isConnected() {
    return state == STATE_CONNECTED;
  }

  private int generateInterval(int k) {
    return (int) (Math.random() * (Math.min(30, (Math.pow(2, k) - 1)) * 1000));
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public void setShouldReconnect(boolean shouldReconnect) {
    this.shouldReconnect = shouldReconnect;
  }

  public @WebSocketState String getState() {
    return state;
  }

  public abstract void connect(final String url);

  public abstract void disconnect(boolean waitForComplete);

  public abstract void send(String msg);

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

  public Observable<String> onConnectError() {
    return onConnectError;
  }
}
