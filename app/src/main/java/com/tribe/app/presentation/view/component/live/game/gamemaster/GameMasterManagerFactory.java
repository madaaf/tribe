package com.tribe.app.presentation.view.component.live.game.gamemaster;

import android.content.Context;
import com.tribe.app.BuildConfig;
import com.tribe.app.data.network.util.TribeApiUtils;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.back.WebSocketConnectionAbs;
import com.tribe.tribelivesdk.back.WebSocketConnectionOkhttp;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 26/03/2018.
 */

public class GameMasterManagerFactory {

  private Context context;
  private WebSocketConnectionOkhttp webSocketConnection;
  private AccessToken accessToken;

  @Inject public GameMasterManagerFactory(Context context,
      @Named("webSocketGameMaster") WebSocketConnectionOkhttp webSocketConnection,
      AccessToken accessToken) {
    this.context = context;
    this.webSocketConnection = webSocketConnection;
    this.accessToken = accessToken;
  }

  public GameMasterManager newInstance() {
    return new GameMasterManager(context, webSocketConnection, accessToken);
  }

  public static class GameMasterManager {

    private Context context;
    private WebSocketConnectionAbs webSocketConnection;
    private Map<String, String> headers;
    private CompositeSubscription subscriptions;

    public GameMasterManager(Context context, WebSocketConnectionOkhttp webSocketConnection,
        AccessToken accessToken) {
      this.context = context;
      this.webSocketConnection = webSocketConnection;
      this.subscriptions = new CompositeSubscription();
      headers = new HashMap<>();

      headers.put(WebSocketConnection.USER_AGENT, TribeApiUtils.getUserAgent(context));
      headers.put(WebSocketConnection.AUTHORIZATION,
          accessToken.getTokenType() + " " + accessToken.getAccessToken());
    }

    public void connect(String sessionId) {
      headers.put(WebSocketConnection.TRIBE_SESSION_ID, "12351");
      webSocketConnection.setHeaders(headers);

      subscriptions.add(
          webSocketConnection.onConnectError().subscribe(s -> Timber.d("onConnectError : " + s)));

      subscriptions.add(
          webSocketConnection.onMessage().subscribe(s -> Timber.d("onMessage : " + s)));

      subscriptions.add(
          webSocketConnection.onStateChanged().subscribe(s -> Timber.d("onStateChanged : " + s)));

      subscriptions.add(webSocketConnection.onError().subscribe(s -> Timber.d("onError : " + s)));

      webSocketConnection.connect(BuildConfig.TRIBE_GAME_MASTER_WSS);
      webSocketConnection.setShouldReconnect(false);
    }

    public void disconnect() {
      subscriptions.clear();

      if (webSocketConnection.getState().equals(WebSocketConnection.STATE_CONNECTED)) {
        webSocketConnection.disconnect(false);
      }

      webSocketConnection.setHeaders(null);
    }

    public void send(String msg) {
      if (webSocketConnection.getState().equals(WebSocketConnection.STATE_CONNECTED)) {
        webSocketConnection.send(msg);
      }
    }

    /**
     * OBSERVABLE
     */

    public Observable<String> onMessage() {
      return webSocketConnection.onMessage();
    }
  }
}
