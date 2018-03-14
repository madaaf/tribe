package com.tribe.app.presentation.view.component.live.game.corona;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.Pair;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ansca.corona.CoronaView;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.presenter.NewChatPresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.mvp.view.adapter.NewChatMVPViewAdapter;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.RemoteConfigManager;
import com.tribe.tribelivesdk.core.WebRTCRoom;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
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

  protected static final String SCORES_KEY = "scores";
  protected static final String CONTEXT_KEY = "context";

  @Inject AccessToken accessToken;

  @Inject GamePresenter gamePresenter;

  @Inject NewChatPresenter newChatPresenter;

  @Inject TagManager tagManager;

  @Inject RxFacebook rxFacebook;

  @BindView(R.id.coronaView) CoronaView coronaView;
  @BindView(R.id.layoutProgress) FrameLayout layoutProgress;
  @BindView(R.id.viewProgress) View viewProgress;
  @BindView(R.id.cardViewProgress) CardView cardViewProgress;

  // VARIABLES
  private Handler mainHandler;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private NewChatMVPViewAdapter newChatMVPViewAdapter;
  private RemoteConfigManager remoteConfigManager;
  private Map<String, Integer> mapScores = new HashMap<>();
  private Score bestScore = null;

  // OBSERVABLES
  private Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> masterMapObs;

  public GameCoronaView(@NonNull Context context, Game game) {
    super(context);

    //FileUtils.getGameUnzippedDir(context).delete();
    //
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public Context context() {
        return getContext();
      }

      @Override public void onUserBestScore(Score score) {
        Hashtable<Object, Object> table = new Hashtable();
        table.put("name", "bestScore");
        table.put("score", (score == null) ? 0 : score.getValue());
        table.put("gameId", game.getId());
        bestScore = score;
        coronaView.sendEvent(table);
      }

      //@Override public void onGameFile(GameFile gameFile) {
      //  Timber.d("GameFile : " + gameFile);
      //
      //  if (gameFile.getDownloadStatus().equals(GameFileRealm.STATUS_DOWNLOADED)) {
      //    subscriptions.add(Observable.timer(1, TimeUnit.SECONDS)
      //        .observeOn(AndroidSchedulers.mainThread())
      //        .subscribe(
      //            aLong -> subscriptions.add(rxUnzip.unzip(gameFile.getPath()).subscribe(path -> {
      //              AnimationUtils.animateWidth(viewProgress, viewProgress.getMeasuredWidth(),
      //                  cardViewProgress.getWidth(), 300, new DecelerateInterpolator());
      //              //coronaView.init(game.getId());
      //              coronaView.init(FileUtils.getGameUnzippedDir(context).getPath());
      //              coronaView.setZOrderMediaOverlay(true);
      //            }))));
      //  } else if (gameFile.getDownloadStatus().equals(GameFileRealm.STATUS_DOWNLOADING)) {
      //    UIUtils.changeWidthOfView(viewProgress,
      //        (int) (((float) gameFile.getTotalSize() / (float) gameFile.getProgress()) *
      //            cardViewProgress.getWidth()));
      //  }
      //}
    };

    newChatMVPViewAdapter = new NewChatMVPViewAdapter() {
      @Override public void onLoadFBContactsInvite(List<Contact> contactList) {
        Timber.d("onLoadFBContactsInvite");
        ArrayList<String> array = new ArrayList<>();
        for (Contact c : contactList) {
          array.add(c.getId());
        }

        Timber.d("Notify FB friends");
        subscriptionsRoom.add(rxFacebook.notifyFriends(context, array).subscribe(aBoolean -> {
          Timber.d("Notify FB answer : " + aBoolean);
          if (aBoolean) successRevive();
          else errorRevive();
        }));
      }

      @Override public void onLoadFBContactsInviteFailed() {
        errorRevive();
      }
    };

    remoteConfigManager = RemoteConfigManager.getInstance(context);

    coronaView.init(game.getId());
    coronaView.setZOrderMediaOverlay(true);

    //gamePresenter.getGameFile(game.getUrl());
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_corona, this, true);
    unbinder = ButterKnife.bind(this);

    layoutProgress.setBackgroundColor(PaletteGrid.getRandomColorExcluding(Color.BLACK));

    mainHandler = new Handler(context.getMainLooper());
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
    newChatPresenter.onViewAttached(newChatMVPViewAdapter);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    gamePresenter.onViewDetached();
    newChatPresenter.onViewDetached();
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    Timber.d("initWebRTCRoomSubscriptions");
    if (webRTCRoom == null) return;
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

          AnimationUtils.animateWidth(viewProgress, viewProgress.getMeasuredWidth(),
              cardViewProgress.getWidth(), 500, new DecelerateInterpolator());

          subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aLong -> AnimationUtils.fadeOut(layoutProgress, 300,
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
      //Timber.d("eventListener fired : " + hashtable);
      String event = (String) hashtable.get("event");
      if (game == null) return null;

      if (event.equals("reviveData")) {
        int disableDurationSec = 10;
        int minScoreTrigger = 10;
        double minRatioTrigger = 0.0D;

        if (!BuildConfig.DEBUG) {
          disableDurationSec =
              remoteConfigManager.getInt(Constants.FIREBASE_REVIVE_DISABLE_DURATION);
          minScoreTrigger = remoteConfigManager.getInt(Constants.FIREBASE_REVIVE_MIN_SCORE_TRIGGER);
          minRatioTrigger =
              remoteConfigManager.getDouble(Constants.FIREBASE_REVIVE_MIN_RATIO_TRIGGER);
        }

        Hashtable<Object, Object> table = new Hashtable();
        table.put("gameId", game.getId());
        table.put("disableDurationSec", disableDurationSec);
        table.put("minScoreTrigger", minScoreTrigger);
        table.put("minRatioTrigger", minRatioTrigger);

        return table;
      } else if (event.equals("getBestScore")) {
        if (currentUser.getScoreForGame(game.getId()) != null) {
          return currentUser.getScoreForGame(game.getId()).getValue();
        } else if (bestScore != null) {
          return bestScore.getValue();
        } else {
          gamePresenter.getUserBestScore(game.getId());
        }
      } else {
        mainHandler.post(() -> {
          if (event.equals("gameLoaded")) {
            startGame();
          } else if (event.equals("saveScore")) {
            onAddScore.onNext(
                Pair.create(game.getId(), ((Double) hashtable.get("score")).intValue()));
          } else if (event.equals("revive")) {
            //if (BuildConfig.DEBUG) {
            //  subscriptionsRoom.add(Observable.timer(1, TimeUnit.SECONDS)
            //      .observeOn(AndroidSchedulers.mainThread())
            //      .subscribe(aLong -> sendSuccessRevive()));
            //} else {
            Timber.d("Revive");
              if (!FacebookUtils.isLoggedIn()) {
                Timber.d("Ask FB login");
                subscriptions.add(rxFacebook.requestLogin().subscribe(loginResult -> {
                  Timber.d("Load contacts");
                  newChatPresenter.loadFBContactsInvite(null);
                }));
              } else {
                Timber.d("Load contacts");
                newChatPresenter.loadFBContactsInvite(null);
              }
            //}
          } else if (event.equals("scoresUpdated")) {
            mapScores.clear();
            Hashtable<String, Double> scores = (Hashtable<String, Double>) hashtable.get("scores");
            for (String id : scores.keySet()) mapScores.put(id, scores.get(id).intValue());
            updateLiveScores(mapScores, null);
          } else if (event.equals("broadcastMessage")) {
            sendMessage((JSONObject) getBroadcastPayload(
                (Hashtable<Object, Object>) hashtable.get("message"), false), null);
          } else if (event.equals("sendMessage")) {
            sendMessage((JSONObject) getBroadcastPayload(
                (Hashtable<Object, Object>) hashtable.get("message"), false),
                hashtable.get("to").toString());
          } else if (event.equals("contextGame")) {
            Timber.d("Hashtable : " + hashtable);
            Hashtable<String, Double> scores =
                (Hashtable<String, Double>) ((Hashtable<Object, Object>) hashtable.get(CONTEXT_KEY))
                    .get(SCORES_KEY);
            Map<String, Integer> contextScores = new HashMap<>();
            for (String key : scores.keySet()) {
              contextScores.put(key, scores.get(key).intValue());
            }
            game.getContextMap().put(SCORES_KEY, contextScores);

            JSONObject jsonObject = getCompleteContextPayload(SCORES_KEY, game.getContextMap());
            webRTCRoom.sendToPeers(jsonObject, true);
          }
        });
      }

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
    if (webRTCRoom == null || game == null) return;

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

  private JSONObject getContextPayload(Map<String, Integer> context) {
    JSONObject obj = new JSONObject();
    JSONObject gameContext = new JSONObject();
    JsonUtils.jsonPut(gameContext, Game.CONTEXT, computeScoreMap(context, true));
    JsonUtils.jsonPut(gameContext, Game.ID, game.getId());
    JsonUtils.jsonPut(obj, WebRTCRoom.MESSAGE_GAME, gameContext);
    return obj;
  }

  private JSONObject getCompleteContextPayload(String key, Map<String, Object> context) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    Map<String, Integer> scoreMap = (Map<String, Integer>) context.get(key);
    JsonUtils.jsonPut(game, key, computeScoreMap(scoreMap, false));
    JsonUtils.jsonPut(game, CONTEXT_KEY, key);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  private JSONObject computeScoreMap(Map<String, Integer> scoreMap, boolean includeId) {
    JSONObject json = new JSONObject();
    JSONObject scoresJson = new JSONObject();

    try {
      for (String scoreKeyId : scoreMap.keySet()) {
        scoresJson.put(scoreKeyId, scoreMap.get(scoreKeyId));
      }

      json.put(SCORES_KEY, scoresJson);
    } catch (JSONException ex) {
      ex.printStackTrace();
    }

    if (includeId) {
      return json;
    } else {
      return scoresJson;
    }
  }

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

  private void sendSuccessRevive() {
    Hashtable<Object, Object> hashtable = new Hashtable<>();
    hashtable.put("name", "shareToReviveSuccess");
    coronaView.sendEvent(hashtable);
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
    coronaView.setCoronaEventListener(null);
    coronaView.destroy();
    super.stop();
  }

  @Override public void dispose() {
    Timber.d("dispose");
    super.dispose();
  }

  @Override public void setNextGame() {

  }

  public void successRevive() {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.NAME, game.getId());
    bundle.putInt(TagManagerUtils.SCORE, mapScores.get(currentUser.getId()));
    tagManager.trackEvent(TagManagerUtils.Revive, bundle);

    sendSuccessRevive();
  }

  public void errorRevive() {
    Hashtable<Object, Object> hashtable = new Hashtable<>();
    hashtable.put("name", "shareToReviveError");
    coronaView.sendEvent(hashtable);
  }

  public void onPause() {
    coronaView.pause();
  }

  /**
   * OBSERVABLES
   */
}
