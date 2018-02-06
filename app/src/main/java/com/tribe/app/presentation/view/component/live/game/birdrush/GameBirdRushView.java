package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import butterknife.BindView;
import butterknife.ButterKnife;
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
import timber.log.Timber;

/**
 * Created by Mada
 */

public class GameBirdRushView extends GameViewWithEngine {

  public static final int HEIGHT_IOS_SCREEN = 667;
  public static final int WIDTH_IOS_SCREEN = 375;

  private static final String BIRD_ACTION_ADD_OBSTACLE = "addObstacles";
  private static final String BIRD_ACTION_PLAYER_TAP = "playerTap";
  private static final String BIRD_KEY_OBSTACLE = "obstacles";

  @BindView(R.id.viewBackground) GameBirdRushBackground viewBackground;

  @Inject ScreenUtils screenUtils;
  @Inject User currentUser;

  // VARIABLE
  private BirdController controller;
  private boolean startedAsSingle = false, didRestartWhenReady = false;

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
    return SoundManager.BIRD_RUSH_SOUNDTRACK;
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
                  Timber.w("SOEF ACTION NEW GAME");
                }
                if (actionKey.equals(BIRD_ACTION_ADD_OBSTACLE)) {
                  JSONArray jsonObstacles = message.getJSONArray(BIRD_KEY_OBSTACLE);
                  List<BirdRushObstacle> obstacles = transform(jsonObstacles);
                  viewBackground.addObstacles(obstacles);
                  Timber.e("add obstacle : " + obstacles.toString());
                } else if (actionKey.equals(BIRD_ACTION_PLAYER_TAP)) {
                  double y;
                  if (message.get(PlayerTap.Y) instanceof Integer) {
                    int y1 = (int) message.get(PlayerTap.Y);
                    y = (double) y1;
                  } else {
                    y = (double) message.get(PlayerTap.Y);
                  }
                  String guestId = message.getString(FROM_KEY);
                  PlayerTap playerTap = new PlayerTap(y);
                  Timber.e("player tap " + playerTap.toString());
                  viewBackground.jumpBird(guestId, playerTap);
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

  protected void setupGameLocally(String userId, Set<String> players, long timestamp) { // SOEF
    Timber.d("SOEF SET UP LOCALLY " + userId + " " + players.size() + " " + timestamp);
    super.setupGameLocally(userId, players, timestamp);
    viewBackground.stop();
  }

  @Override protected void gameOver(String winnerId, boolean isLocal) {
    Timber.d("SOEF Game Bird Rush Over : " + winnerId);
    super.gameOver(winnerId, isLocal);
  }

  private void overcomeObstacle() {
    if (!pending) {
      soundManager.playSound(SoundManager.BIRD_RUSH_OBSTACLE, SoundManager.SOUND_MAX);
      addPoints(1, currentUser.getId(), true);
      if (roundPoints > 20) {
        ((GameBirdRushEngine) gameEngine).setLevel(GameBirdRushEngine.Level.ALIEN);
      } else if (roundPoints > 10) {
        ((GameBirdRushEngine) gameEngine).setLevel(GameBirdRushEngine.Level.EXTREME);
      } else if (roundPoints > 5) {
        ((GameBirdRushEngine) gameEngine).setLevel(GameBirdRushEngine.Level.HARD);
      } else {
        ((GameBirdRushEngine) gameEngine).setLevel(GameBirdRushEngine.Level.MEDIUM);
      }
    }
  }

  protected void playGame() {
    subscriptions.add(Observable.timer((1500), TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          Timber.d("SOEF playGame");
          super.playGame();
          setOnTouchListener(controller);
          viewBackground.start();
        }));
  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();
    Timber.d(" SOEF start master engine myBird rush ");

    subscriptionsSession.add(
        ((GameBirdRushEngine) gameEngine).onObstacle().subscribe(generateObstacleList -> {
          webRTCRoom.sendToPeers(getObstaclePayload(generateObstacleList), true);
          Timber.e("SOEF POP ISTAVLE received " + getObstaclePayload(generateObstacleList));
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
    setOnTouchListener(null);
    iLost();
  }

  private void initSubscriptions() {

    subscriptions.add(viewBackground.onGameOver().subscribe(aVoid -> {
      gameOver();
    }));

    subscriptions.add(viewBackground.onAddPoint().subscribe(aVoid -> {
      overcomeObstacle();
    }));

    subscriptions.add(controller.onTap().subscribe(aVoid -> {
      soundManager.playSound(SoundManager.BIRD_RUSH_TAP, SoundManager.SOUND_MAX);
      webRTCRoom.sendToPeers(
          getTapPayload(viewBackground.getMyBird().getX(), viewBackground.getMyBird().getY()),
          true);
      //Timber.w("SOEF TAP " + getTapPayload(viewBackground.getMyBird().getX(), viewBackground.getMyBird().getY()));
      viewBackground.jumpBird(currentUser.getId(), null);
    }));
  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    Timber.d(" SOEF on start myBird Rush");
    wordingPrefix = "game_bird_rush_";
    super.start(game, mapObservable, liveViewsObservable, userId);

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> imReady()));

    subscriptions.add(mapObservable.subscribe(peerMap -> {
      int index = 0;
      for (String key : peerMap.keySet()) {
        TribeGuest guest = peerMap.get(key);
        Timber.e(" SOEF ADD GUEST" + peerMap.get(key) + " " + peerMap.size());
        BirdRush bird = new BirdRush(index, guest, screenUtils, currentUser.getId());
        if (!viewBackground.haveBird(peerMap.get(key))) {
          viewBackground.addBird(bird, index);
          index++;
        }
      }
    }));
  }

  @Override public void stop() {
    super.stop();
    Timber.d(" SOEF on stop game bird rush view");
  }

  @Override public void dispose() {
    super.dispose();
    Timber.d(" SOEF on dispose");
    viewBackground.dispose();
  }

  @Override public void setNextGame() {
    Timber.e(" SOEF setNextGame");
  }

  /**
   * JSON PAYLOAD
   */
  private JSONObject getTapPayload(float x, float y) {
    JSONObject obj = new JSONObject();
    JSONObject tap = new JSONObject();
    JsonUtils.jsonPut(tap, ACTION_KEY, BIRD_ACTION_PLAYER_TAP);
    JsonUtils.jsonPut(tap, FROM_KEY, currentUser.getId());
    JsonUtils.jsonPut(tap, "x", WIDTH_IOS_SCREEN / 2); // IOS RETRO-COMPATIBILITY
    JsonUtils.jsonPut(tap, "y", HEIGHT_IOS_SCREEN - y);
    JsonUtils.jsonPut(obj, this.game.getId(), tap);
    return obj;
  }

  private JSONObject getObstaclePayload(List<BirdRushObstacle> list) {
    JSONObject obj = new JSONObject();
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    for (BirdRushObstacle o : list) {
      array.put(o.obstacleAsJSON());
    }
    JsonUtils.jsonPut(jsonObject, ACTION_KEY, BIRD_ACTION_ADD_OBSTACLE);
    JsonUtils.jsonPut(jsonObject, BIRD_KEY_OBSTACLE, array);
    JsonUtils.jsonPut(obj, this.game.getId(), jsonObject);
    return obj;
  }

  private ArrayList<BirdRushObstacle> transform(JSONArray jArray) throws JSONException {
    ArrayList<BirdRushObstacle> listdata = new ArrayList<>();
    if (jArray != null) {
      for (int i = 0; i < jArray.length(); i++) {
        JSONObject json = (JSONObject) jArray.get(i);
        BirdRushObstacle obj = BirdRushObstacle.jsonAsObstacle(json, screenUtils.getWidthPx(),
            screenUtils.getHeightPx());
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
