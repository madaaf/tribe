package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.gson.Gson;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithEngine;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by Mada
 */

public class GameBirdRushView extends GameViewWithEngine {
  public static Long SPEED_BACK_SCROLL;

  private static final String BIRD_ACTION_ADD_OBSTACLE = "addObstacles";
  private static final String BIRD_ACTION_PLAYER_TAP = "playerTap";
  private static final String BIRD_KEY_OBSTACLE = "obstacles";

  @BindView(R.id.viewBackground) GameBirdRushBackground viewBackground;
  @BindView(R.id.viewBirds) FrameLayout viewBirds;

  @Inject ScreenUtils screenUtils;
  @Inject User currentUser;

  // VARIABLE
  private BirdController controller;
  private boolean startedAsSingle = false, didRestartWhenReady = false, gameOver = false;

  // OBSERVABLES
  protected CompositeSubscription subscriptions;

  // RESSOURCE
  private Map<TribeGuest, BirdRush> birdsList = new HashMap<>();

  public GameBirdRushView(@NonNull Context context) {
    super(context);
  }

  public GameBirdRushView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);
    inflater.inflate(R.layout.view_game_bird_rush, this, true);
    unbinder = ButterKnife.bind(this);
    controller = new BirdController(context);

    setOnTouchListener(controller);
    initSubscriptions();
  }

  @Override protected GameEngine generateEngine() {
    return new GameBirdRushEngine(context, GameBirdRushEngine.Level.MEDIUM, screenUtils);
  }

  @Override protected int getSoundtrack() {
    return SoundManager.ALIENS_ATTACK_SOUNDTRACK;
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          if (pair.second.has(game.getId())) {
            try {
              JSONObject message = pair.second.getJSONObject(game.getId());
              Timber.e("SUBS BIRD " + message);
              if (message.has(ACTION_KEY)) {
                String actionKey = message.getString(ACTION_KEY);
                if (actionKey.equals(ACTION_NEW_GAME)) {
                  viewBackground.clearObstacles();
                }
                if (actionKey.equals(BIRD_ACTION_ADD_OBSTACLE)) {
                  JSONArray jsonObstacles = message.getJSONArray(BIRD_KEY_OBSTACLE);
                  List<BirdRushObstacle> obstacles = transform(jsonObstacles);
                  viewBackground.addObstacles(obstacles);
                  Timber.e("add obstacle : " + obstacles.toString());
                } else if (actionKey.equals(BIRD_ACTION_PLAYER_TAP)) {
                  PlayerTap playerTap =
                      new PlayerTap((Double) message.get("x"), (Double) message.get("y"));

                  Timber.e("player tap : " + playerTap.toString());
                } else {
                  Timber.e("SOEF ANOTHER ACTION  " + actionKey);
                }
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }));
  }

  protected void playGame() {
    Timber.e("SOEF playGame");
    super.playGame();
    viewBackground.start();
    //  startBirdAnimation();
  }

  protected void setupGameLocally(String userId, Set<String> players, long timestamp) { // SOEF
    Timber.d("SOEF SET UP LOCALLY " + userId + " " + players.size() + " " + timestamp);
    super.setupGameLocally(userId, players, timestamp);
    //  viewBackground.removeObstacles();

    subscriptionsSession.add(onPending.subscribe(aBoolean -> {
      for (int i = 0; i < viewBirds.getChildCount(); i++) {
        /*
        if (viewBirds.getChildAt(i) instanceof BirdRush) {
          BirdRush birdView = (BirdRush) viewBirds.getChildAt(i);
          float alpha = aBoolean && !birdView.isLost() ? 0.5f : 1f;
          birdView.setAlpha(alpha);
        }*/
      }
    }));
  }

  @Override protected void gameOver(String winnerId, boolean isLocal) {
    Timber.e("SOEF Game Bird Rush Over : " + winnerId);
    super.gameOver(winnerId, isLocal);
    //initBirds();
    viewBackground.stop();
  }

  private void overcomeObstacle() {
    if (!pending) {
      addPoints(1, currentUser.getId(), true);
    }
  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();
    Timber.e(" SOEF start master engine myBird rush ");

    subscriptionsSession.add(
        ((GameBirdRushEngine) gameEngine).onObstacle().subscribe(generateObstacleList -> {
          Timber.e("SOEF send to peer obtsacle " + generateObstacleList.size());
          webRTCRoom.sendToPeers(getObstaclePayload(generateObstacleList), true);
          viewBackground.addObstacles(generateObstacleList);
        }));

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          didRestartWhenReady = false;

          Map<String, Integer> mapPlayerStatus = gameEngine.getMapPlayerStatus();
          int countPlaying = 0;
          for (Integer i : mapPlayerStatus.values()) if (i == GameEngine.PLAYING) countPlaying++;

          startedAsSingle = gameEngine != null && countPlaying == 1;

          subscriptions.add(gameEngine.onPlayerReady().subscribe(userId -> {
            if (!didRestartWhenReady && startedAsSingle) {
              if (mapPlayerStatus.size() == 2) {
                didRestartWhenReady = true;
                startedAsSingle = false;

                subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong1 -> {
                      resetScores(true);
                      iLost();
                    }));
              }
            }
          }));
        }));
  }

  private void gameOver() {
    Timber.e("SOEF LOCAL game over");
    gameOver = true;
    setOnTouchListener(null);
    resetScores(true);
    iLost();
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();

    subscriptions.add(viewBackground.onGameOver().subscribe(aVoid -> {
      gameOver();
    }));

    subscriptions.add(controller.onTap().subscribe(aVoid -> { // MADA
      webRTCRoom.sendToPeers(
          getTapPayload(viewBackground.getMyBird().getX(), viewBackground.getMyBird().getY()),
          true);
      Timber.w("SOEF GET TAP PLAYLOAD" + getTapPayload(viewBackground.getMyBird().getX(),
          viewBackground.getMyBird().getY()));
      viewBackground.jumpBird(viewBackground.getMyBird());
    }));
  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    Timber.e(" SOEF on start myBird Rush");
    wordingPrefix = "game_bird_rush_";
    gameOver = false;
    super.start(game, mapObservable, liveViewsObservable, userId);

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> imReady()));

    subscriptions.add(mapObservable.subscribe(peerMap -> {
      int index = 1;
      for (String key : peerMap.keySet()) {
        TribeGuest guest = peerMap.get(key);
        Timber.e(" SOEF ADD GUEST" + peerMap.get(key) + " " + peerMap.size());
        BirdRush bird = new BirdRush(index, guest, screenUtils, currentUser.getId());
        if (!viewBackground.haveBird(peerMap.get(key))) {
          viewBackground.addBird(bird);
          index++;
        }
      }
    }));
  }

  /*
  private boolean haveBird(TribeGuest tribeGuest) {
    for (int i = 0; i < viewBirds.getChildCount(); i++) {
      if (viewBirds.getChildAt(i) instanceof BirdRush) {
        BirdRush birdView = (BirdRush) viewBirds.getChildAt(i);
        if (birdView.getGuestId().equals(tribeGuest.getId())) {
          return true;
        }
      }
    }
    return false;
  }*/

  @Override public void stop() {
    super.stop();
    Timber.e(" SOEF on stop");
    viewBackground.stop();
  }

  @Override public void dispose() {
    super.dispose();
    Timber.e(" SOEF on dispose");
    viewBackground.dispose();
    viewBirds.removeAllViews();
    subscriptions.unsubscribe();
  }

  @Override public void setNextGame() {
    Timber.e(" SOEF setNextGame");
  }

  /**
   * JSON PAYLOAD
   */
  private JSONObject getTapPayload(float x, float y) {
    JSONObject tap = new JSONObject();
    JsonUtils.jsonPut(tap, ACTION_KEY, BIRD_ACTION_PLAYER_TAP);
    JsonUtils.jsonPut(tap, "x", x);
    JsonUtils.jsonPut(tap, "y", y);
    return tap;
  }

  private JSONObject getObstaclePayload(List<BirdRushObstacle> list) {
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    for (BirdRushObstacle o : list) {
      array.put(o.obstacleAsJSON());
    }
    JsonUtils.jsonPut(jsonObject, ACTION_KEY, BIRD_ACTION_ADD_OBSTACLE);
    JsonUtils.jsonPut(jsonObject, BIRD_KEY_OBSTACLE, array);
    return jsonObject;
  }

  private ArrayList<BirdRushObstacle> transform(JSONArray jArray) throws JSONException {
    ArrayList<BirdRushObstacle> listdata = new ArrayList<>();
    if (jArray != null) {
      for (int i = 0; i < jArray.length(); i++) {
        BirdRushObstacle obj =
            new Gson().fromJson(String.valueOf(jArray.get(i)), BirdRushObstacle.class);
        listdata.add(obj);
      }
    }
    return listdata;
  }

  /**
   * OBSERVABLES
   */

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }
}
