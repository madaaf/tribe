package com.tribe.app.presentation.view.component.live.game.corona;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import com.ansca.corona.CoronaView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by nicolasbradier on 20/02/2018.
 */

public class GameCoronaViewOld extends GameView {

  protected static final String ACTION_NEW_GAME = "newGame";
  private static final String ACTION_USER_GAME_OVER = "userGameOver";
  private static final String ACTION_USER_WAITING = "userWaiting";
  private static final String ACTION_USER_READY = "userReady";
  private static final String ACTION_SHOW_USER_LOST = "showUserLost";
  private static final String ACTION_GAME_OVER = "gameOver";

  private static final String PLAYERS = "players";
  private static final String TIMESTAMP = "timestamp";

  public com.ansca.corona.CoronaView coronaView;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GameCoronaViewOld(@NonNull Context context) {
    super(context);
  }

  public GameCoronaViewOld(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_corona, this, true);
    //unbinder = ButterKnife.bind(this);
    coronaView = findViewById(R.id.coronaAnscaView);
  }

  public void setup() {
    coronaView.init("coronatest/aliens-attack");
    coronaView.setZOrderMediaOverlay(true);
  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> map,
      Observable<Map<String, TribeGuest>> mapInvited,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    super.start(game, map, mapInvited, liveViewsObservable, userId);

    setup();

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {

          coronaView.resume();

          becomePlayer();
          if (userId.equals(currentUser.getId())) becomeGameMaster();
        }));

    /*
    subscriptions.add(Observable.interval(1000, 1000, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          Timber.d("I'M ALIVE");
        }));
        */

    //if (getSoundtrack() != -1) soundManager.playSound(getSoundtrack(), SoundManager.SOUND_MAX);
  }

  protected void becomePlayer() {
    Log.d("NICO", "becomePlayer");
    // if (subscriptionsSession != null) subscriptionsSession.clear();
    // stopEngine();
    // startEngine();
  }

  protected void becomeGameMaster() {
    Log.d("NICO", "becomeGameMaster");

    // startMasterEngine();
    // Map<String, Integer> mapPlayerStatus = gameEngine.getMapPlayerStatus();
    Map<String, Integer> playerIds = new HashMap<>();
    playerIds.put(currentUser.getId(), 0);

    newGame(playerIds.keySet());
  }

  protected void newGame(Set<String> playerIds) {
    Timber.d("newGame");
    long timestamp = startGameTimestamp();
    webRTCRoom.sendToPeers(getNewGamePayload(currentUser.getId(), timestamp,
        playerIds.toArray(new String[playerIds.size()])), true);

    // setupGameLocally(currentUser.getId(), playerIds, timestamp);
    // resetScores(false);

    Hashtable<Object, Object> event = new Hashtable<>();
    Hashtable<Object, Object> playersUsers = new Hashtable<>();
    Hashtable<Object, Object> meAsCoronaUser = new Hashtable<>();
    meAsCoronaUser.put("id", currentUser.getId());
    meAsCoronaUser.put("displayName", currentUser.getDisplayName());
    meAsCoronaUser.put("username", currentUser.getUsername());
    meAsCoronaUser.put("picture", currentUser.getProfilePicture());

    playersUsers.put(1, meAsCoronaUser);
    event.put("name", "startGame");
    event.put("myUserId", currentUser.getId());
    event.put("masterUserId", currentUser.getId());
    event.put("playersUsers", playersUsers);

    //Timber.d("event " + playersUsers);
    coronaView.sendEvent(event);

    coronaView.setCoronaEventListener(new CoronaView.CoronaEventListener() {
      @Override public Object onReceivedCoronaEvent(CoronaView coronaView,
          Hashtable<Object, Object> hashtable) {
        //Timber.d("onReceivedCoronaEvent - " + hashtable);
        if (hashtable.containsValue("gameLoaded")) {
          // TODO
        }
        return null;
      }
    });
  }

  protected long startGameTimestamp() {
    Log.d("NICO", "startGameTimestamp");
    return System.currentTimeMillis() + 5 * 1000;
  }

  private JSONObject getNewGamePayload(String userId, long timestamp, String[] playerIds) {
    Log.d("NICO", "getNewGamePayload");
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_NEW_GAME);
    JsonUtils.jsonPut(game, FROM_KEY, userId);
    JSONArray jsonArray = new JSONArray();
    for (String id : playerIds) jsonArray.put(id);
    JsonUtils.jsonPut(game, PLAYERS, jsonArray);
    JsonUtils.jsonPut(game, TIMESTAMP, Long.valueOf(timestamp).doubleValue() / 1000);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    Log.d("NICO", "initWebRTCRoomSubscriptions");
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> receiveMessage(pair.first, pair.second)));
  }

  protected void receiveMessage(TribeSession tribeSession, JSONObject jsonObject) {
    Log.d("NICO", "receiveMessage " + tribeSession.toString() + " - " + jsonObject);
    if (jsonObject.has(game.getId())) {
      try {
        JSONObject message = jsonObject.getJSONObject(game.getId());
        Hashtable<Object, Object> event = new Hashtable<>();
        event.put("name", "receiveMessage");
        event.put("message", message);
        event.put("fromUserId", currentUser.getId());

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override public void run() {

            AsyncTask.execute(new Runnable() {
              @Override public void run() {
                coronaView.sendEvent(event);
              }
            });
          }
        }, 5000);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  @Override protected void takeOverGame() {

  }

  @Override public void setNextGame() {

  }

  @Override public void stop() {
    super.stop();
    coronaView.destroy();
  }
}
