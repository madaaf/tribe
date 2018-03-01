package com.tribe.app.presentation.view.component.live.game.corona;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ansca.corona.CoronaView;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by tiago on 11/13/2017.
 */

public class GameCoronaView extends GameView {

  @Inject AccessToken accessToken;

  @BindView(R.id.coronaView) CoronaView coronaView;
  @BindView(R.id.layoutProgress) FrameLayout layoutProgress;
  @BindView(R.id.viewProgress) View viewProgress;
  @BindView(R.id.cardViewProgress) CardView cardViewProgress;

  // VARIABLES
  private Handler mainHandler;



  // OBSERVABLES
  private Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> masterMapObs;

  public GameCoronaView(@NonNull Context context, Game game) {
    super(context);
    coronaView.init("coronatest/aliens-attack");
    coronaView.setZOrderMediaOverlay(true);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_corona, this, true);
    unbinder = ButterKnife.bind(this);

    mainHandler = new Handler(context.getMainLooper());
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    Timber.d("initWebRTCRoomSubscriptions");
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> receiveMessage(pair.first, pair.second)));
  }

  @Override protected void initDependencyInjector() {
    super.initDependencyInjector();
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  @Override protected void takeOverGame() {

  }

  private Observable<Hashtable<Object, Hashtable<Object, Object>>> playersForCorona() {
    return Observable.just(peerMap).map(stringTribeGuestMap -> {
      Hashtable<Object, Hashtable<Object, Object>> coronaPlayers = new Hashtable<>();

      int i = 1;
      for (TribeGuest guest : stringTribeGuestMap.values()) {
        if (guest.canPlayGames(game.getId())) {
          coronaPlayers.put(i, guest.asCoronaUser());
          i++;
        }
      }

      return coronaPlayers;
    });
  }

  private void startGame() {
    Timber.d("startGame");
    coronaView.resume();

    subscriptions.add(playersForCorona().observeOn(AndroidSchedulers.mainThread())
        .single()
        .subscribe(coronaUsers -> {
          Hashtable<Object, Object> startGameTable = new Hashtable<>();
          startGameTable.put("name", "startGame");
          startGameTable.put("myUserId", currentUser.getId());
          startGameTable.put("masterUserId", currentMasterId);
          startGameTable.put("playersUsers", coronaUsers);
          //startGameTable.put("roomId", "");
          startGameTable.put("bearer", accessToken.getAccessToken());
          startGameTable.put("isVolumeEnabled", true);

          coronaView.sendEvent(startGameTable);
          subscriptions.add(Observable.timer(2000, TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aLong -> AnimationUtils.fadeOut(layoutProgress, 250,
                  new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                      if (layoutProgress.getParent() != null) removeView(layoutProgress);
                    }
                  })));
        }));

    subscriptions.add(masterMapObs.subscribe(rxHashMapAction -> {
      if (rxHashMapAction.changeType.equals(ObservableRxHashMap.ADD) &&
          rxHashMapAction.item.canPlayGames(game.getId())) {
        Hashtable<Object, Object> userJoinedTable = new Hashtable<>();
        userJoinedTable.put("name", "userJoined");
        userJoinedTable.put("user", rxHashMapAction.item.asCoronaUser());
        coronaView.sendEvent(userJoinedTable);
      } else if (rxHashMapAction.changeType.equals(ObservableRxHashMap.REMOVE)) {
        Hashtable<Object, Object> userLeftTable = new Hashtable<>();
        userLeftTable.put("name", "userLeft");
        userLeftTable.put("user", rxHashMapAction.item.getId());
        coronaView.sendEvent(userLeftTable);
      }
    }));
  }

  private void runGame() {
    Timber.d("runGame");
    coronaView.setCoronaEventListener((coronaView, hashtable) -> {
      Timber.d("eventListener fired : " + hashtable);
      String event = (String) hashtable.get("event");

      mainHandler.post(() -> {
        if (event.equals("gameLoaded")) {
          startGame();
        } else if (event.equals("saveScore")) {
          onAddScore.onNext(
              Pair.create(game.getId(), ((Double) hashtable.get("score")).intValue()));
        } else if (event.equals("scoresUpdated")) {
          Map<String, Integer> mapScores = new HashMap<>();
          Hashtable<String, Double> scores = (Hashtable<String, Double>) hashtable.get("scores");
          for (String id : scores.keySet()) mapScores.put(id, scores.get(id).intValue());
          updateLiveScores(mapScores, null);
        } else if (event.equals("broadcastMessage")) {
          sendMessage(
              (JSONObject) getBroadcastPayload((Hashtable<Object, Object>) hashtable.get("message"),
                  false), null);
        } else if (event.equals("sendMessage")) {
          sendMessage(
              (JSONObject) getBroadcastPayload((Hashtable<Object, Object>) hashtable.get("message"),
                  false), hashtable.get("to").toString());
        }
      });

      return null;
    });
  }

  @Override protected void receiveMessage(TribeSession tribeSession, JSONObject jsonObject) {
    Timber.d("receiveMessage");
    super.receiveMessage(tribeSession, jsonObject);

    if (jsonObject.has(game.getId())) {
      try {
        JSONObject message = jsonObject.getJSONObject(game.getId());
        Hashtable<Object, Object> table = new Hashtable();
        table.put("name", "receiveMessage");
        table.put("message", JsonHelper.toHashtable(message));
        table.put("fromUserId", tribeSession.getUserId());
        coronaView.sendEvent(table);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  private void sendMessage(JSONObject obj, String id) {
    JSONObject message = new JSONObject();
    JsonUtils.jsonPut(message, this.game.getId(), obj);
    if (StringUtils.isEmpty(id)) {
      webRTCRoom.sendToPeers(message, true);
    } else {
      webRTCRoom.sendToUser(id, message, true);
    }
  }

  /**
   * JSON PAYLOAD
   */

  private Object getBroadcastPayload(Hashtable<Object, Object> hashtable, boolean isArray) {
    if (isArray) {
      JSONArray array = new JSONArray();

      for (Object value : hashtable.values()) {
        array.put(value);
      }

      return array;
    } else {
      JSONObject obj = new JSONObject();

      for (Object key : hashtable.keySet()) {
        String keyStr = (String) key;
        Object object = hashtable.get(keyStr);

        if (object instanceof Hashtable) {
          JsonUtils.jsonPut(obj, keyStr, getBroadcastPayload((Hashtable<Object, Object>) object,
              keyStr.toString().equals("players")));
        } else if (object instanceof Double) {
          JsonUtils.jsonPut(obj, keyStr, ((Double) object).floatValue());
        } else if (object instanceof String) {
          JsonUtils.jsonPut(obj, keyStr, object.toString());
        }
      }

      return obj;
    }
  }

  /**
   * PUBLIC
   */

  @Override public void start(Game game,
      Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> masterMapObs,
      Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, TribeGuest>> mapInvitedObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    Timber.d("start");
    super.start(game, masterMapObs, mapObservable, mapInvitedObservable, liveViewsObservable,
        userId);
    this.masterMapObs = masterMapObs;
    currentMasterId = userId;
    game.setCurrentMaster(peerMap.get(userId));
    runGame();
  }

  @Override public void stop() {
    super.stop();
  }

  @Override public void dispose() {
    Timber.d("dispose");
    super.dispose();
    coronaView.destroy();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */
}
