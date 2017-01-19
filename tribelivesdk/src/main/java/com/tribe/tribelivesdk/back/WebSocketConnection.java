package com.tribe.tribelivesdk.back;

import android.support.annotation.StringDef;

import com.tribe.tribelivesdk.util.LogUtil;

import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;

import rx.Observable;
import rx.subjects.PublishSubject;

@Singleton
public class WebSocketConnection {

    private static final int CLOSE_TIMEOUT = 1000;

    @StringDef({NEW, CONNECTING, CONNECTED, DISCONNECTED, ERROR})
    public @interface WebSocketState {}

    public static final String NEW = "new";
    public static final String CONNECTING = "connecting";
    public static final String CONNECTED = "connected";
    public static final String DISCONNECTED = "disconnected";
    public static final String ERROR = "error";

    @StringDef({JOIN, OFFER, CANDIDATE, LEAVE})
    public @interface WebSocketMessageType {}

    public static final String JOIN = "joinR";
    public static final String OFFER = "exchangeSdp";
    public static final String CANDIDATE = "exchangeFrom";
    public static final String LEAVE = "leave";

    private @WebSocketState String state;
    private WebSocketClient webSocketClient;
    private final Object closeLock = new Object();
    private boolean close;

    private PublishSubject<String> onStateChanged = PublishSubject.create();
    private PublishSubject<String> onMessage = PublishSubject.create();
    private PublishSubject<String> onError = PublishSubject.create();

    @Inject
    public WebSocketConnection() {
        state = NEW;
    }

    public void connect(final String url) {
        if (state == CONNECTED) {
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

        webSocketClient = new WebSocketClient(uri, new Draft_17()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                LogUtil.d(getClass(), "WebSocket connection opened to: " + url);
                state = CONNECTED;
                onStateChanged.onNext(state);
            }

            @Override
            public void onMessage(String s) {
                if (state == CONNECTED) {
                    onMessage.onNext(s);
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                LogUtil.d(getClass(), "Closed " + s + " state : " + state);

                synchronized (closeLock) {
                    close = true;
                    closeLock.notify();
                }

                if (state != DISCONNECTED) {
                    state = DISCONNECTED;
                    onStateChanged.onNext(state);
                }
            }

            @Override
            public void onError(Exception e) {
                LogUtil.e(getClass(), "Error " + e.getMessage());

                state = ERROR;
                onError.onNext(e.getMessage());
            }

            @Override
            public void onClosing(int code, String reason, boolean remote) {
                LogUtil.d(getClass(), "Code : " + code + ", Reason : " + reason + ", Remote : " + remote);
            }
        };

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

        webSocketClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
        webSocketClient.connect();
    }

    private boolean isConnected() {
        return state == CONNECTED;
    }

    void disconnect(boolean waitForComplete) {
        LogUtil.e(getClass(), "Disconnect");

        if (webSocketClient != null) {
            state = DISCONNECTED;
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

    public static @WebSocketMessageType String getWebSocketMessageType(String a) {
        if (a.equals(JOIN)) {
            return JOIN;
        } else if (a.equals(OFFER)) {
            return OFFER;
        } else if (a.equals(CANDIDATE)) {
            return CANDIDATE;
        } else {
            return LEAVE;
        }
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
