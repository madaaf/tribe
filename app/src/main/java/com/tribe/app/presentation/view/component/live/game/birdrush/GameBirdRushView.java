package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.gson.Gson;
import com.tribe.app.R;
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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by Mada
 */

public class GameBirdRushView extends GameViewWithEngine {

  private static final String BIRD_ACTION_ADD_OBSTACLE = "addObstacles";
  private static final String BIRD_ACTION_PLAYER_TAP = "playerTap";
  private static final String BIRD_KEY_OBSTACLE = "obstacles";

  @BindView(R.id.viewBackground) GameBirdRushBackground viewBackground;
  @BindView(R.id.viewBirds) FrameLayout viewBirds;
  @BindView(R.id.bird) ImageView bird;

  @Inject ScreenUtils screenUtils;

  public static Long SPEED_BACK_SCROLL;
  private BirdController controller;
  private Double delay = null;
  private Map<TribeGuest, ImageView> birdsList = new HashMap<>();
  private boolean startedAsSingle = false, didRestartWhenReady = false, gameOver = false;

  int i = 0;
  private List<BirdRushObstacle> obstaclesList = new ArrayList<>();

  private Integer[] birdsImage = new Integer[] {
      R.drawable.game_bird1, R.drawable.game_bird2, R.drawable.game_bird3, R.drawable.game_bird4,
      R.drawable.game_bird5, R.drawable.game_bird6, R.drawable.game_bird7, R.drawable.game_bird8
  };

  // OBSERVABLES
  protected CompositeSubscription subscriptions;
  private Subscription timer;
  private Map<BirdRushObstacle, ImageView> obstacleVisibleScreen = new HashMap<>();

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

    initRessource();
    initSubscriptions();
    setTimer();
  }

  private void initRessource() {
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        SPEED_BACK_SCROLL = Long.valueOf(screenUtils.getWidthPx() / 120) * 1000;
      }
    });
  }

  @Override protected GameEngine generateEngine() {
    return new GameBirdRushEngine(context, GameBirdRushEngine.Level.MEDIUM);
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
              if (message.has(ACTION_KEY)) {
                String actionKey = message.getString(ACTION_KEY);
                if (actionKey.equals(ACTION_NEW_GAME)) {
                  obstaclesList.clear();
                }
                if (actionKey.equals(BIRD_ACTION_ADD_OBSTACLE)) {
                  JSONArray jsonObstacles = message.getJSONArray(BIRD_KEY_OBSTACLE);
                  List<BirdRushObstacle> obstacles = transform(jsonObstacles);
                  obstaclesList.addAll(obstacles);
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

  private void setTimer() {
    if (timer == null) {
      Timber.e("SOEF NEW TIMEER RESET");
      subscriptions.add(timer = Observable.interval(3000, 100, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            animateObstacleList(aLong);
            handleCollisionWithObstacle();
          }));
    }
  }

  private void resetTimer() {
    Timber.e("SOEF RESET TIMER");
    displayFirstObstacle = false;
    if (timer != null) timer.unsubscribe();
    timer = null;
  }

  boolean displayFirstObstacle = false;

  private void animateObstacleList(Long aLong) {
    Timber.e(" ON TOME : " + aLong + " " + obstaclesList.size());
    if (obstaclesList != null && !obstaclesList.isEmpty()) {
      //if (gameOver) return;
      aLong = aLong * 100;
      // init first obstacle
      if (!displayFirstObstacle) {
        displayFirstObstacle = true;
        obs = obstaclesList.get(0);
        delay = ((obs.getNextSpawn() * 1000) + aLong);
        Timber.e("SOEF ANIMATE FIRST  OBSC "
            + aLong
            + " "
            + i
            + " "
            + obs.toString()
            + obstaclesList.size());
        animateObstacle(obs);
        i++;
      }

      // init next obstacle
      if (delay != null && (delay == aLong.doubleValue())) {
        i++;
        obs = obstaclesList.get(i);
        delay = ((obs.getNextSpawn() * 1000) + aLong);
        Timber.e("SOEF ANIMATE OBSC "
            + aLong
            + " "
            + i
            + " "
            + obs.toString()
            + " "
            + obstaclesList.size());
        animateObstacle(obs);
      }

      handleCollisionWithObstacle();
    }
  }

  protected void playGame() {
    Timber.e("SOEF playGame");
    super.playGame();
    viewBackground.start();
  }

  protected void setupGameLocally(String userId, Set<String> players, long timestamp) { // SOEF
    Timber.d("SOEF SET UP LOCALLY " + userId + " " + players.size() + " " + timestamp);
    super.setupGameLocally(userId, players, timestamp);
    subscriptionsSession.add(onPending.subscribe(aBoolean -> {
      for (int i = 0; i < viewBirds.getChildCount(); i++) {
       /* if (viewBirds.getChildAt(i) instanceof GameAliensAttackAlienView) {
          GameAliensAttackAlienView alienView =
              (GameAliensAttackAlienView) viewAliens.getChildAt(i);
          float alpha = aBoolean && !alienView.isLost() ? 0.5f : 1f;
          alienView.setAlpha(alpha);
        }*/
      }
    }));
  }

  @Override protected void gameOver(String winnerId, boolean isLocal) {
    Timber.e("SOEF Game Bird Rush Over : " + winnerId);
    super.gameOver(winnerId, isLocal);
    viewBirds.removeAllViews();
    viewBackground.stop(obstacleVisibleScreen);

    resetTimer();
  }

  private void overcomeObstacle() {
    if (!pending) {
      addPoints(1, currentUser.getId(), true);
    }
  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();
    Timber.e(" SOEF start master engine bird rush ");

    subscriptionsSession.add(
        ((GameBirdRushEngine) gameEngine).onObstacle().subscribe(generateObstacleList -> {
          Timber.e("SOEF send to peer obtsacle " + generateObstacleList.size());
          webRTCRoom.sendToPeers(getObstaclePayload(generateObstacleList), true);
          obstaclesList.addAll(generateObstacleList);
          // animateObstacle(obstacle);
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

  BirdRushObstacle obs = null;

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
  }

  private void handleCollisionWithObstacle() {
    if (obstacleVisibleScreen != null && !obstacleVisibleScreen.isEmpty()) {
      for (Map.Entry<BirdRushObstacle, ImageView> entry : obstacleVisibleScreen.entrySet()) {
        BirdRushObstacle b = entry.getKey();
        ImageView obsclView = entry.getValue();

        if (obsclView != null
            && b != null
            && obsclView.getX() > 0
            && obsclView.getX() > screenUtils.getWidthPx() / 2) {

          if (obsclView.getX() - obsclView.getWidth() < (screenUtils.getWidthPx() / 2)) {
            if (isBetween(obsclView.getY(), obsclView.getY() + obsclView.getHeight(),
                bird.getY())) {
              gameOver(obsclView);
            }
          }
        }
      }
    }
  }

  private void gameOver(ImageView obstacle) {
    Timber.e("SOEF LOCAL game over");
    gameOver = true;
    setOnTouchListener(null);
    fallBird();
    // obstacle.getAnimation().cancel();
    obstacle.clearAnimation();
    obstacle.setImageDrawable(
        ContextCompat.getDrawable(context, R.drawable.game_birdrush_obstacle_red));

    resetScores(true);
    iLost();
  }

  private boolean isBetween(float x1, float x2, float pos) {
    if (pos > x1 && pos < x2) {
      return true;
    }
    return false;
  }

  private void animateObstacle(BirdRushObstacle model) {
    float height = (model.getStart() * screenUtils.getHeightPx() - model.getView().getHeight() / 2);
    ImageView obstacle = model.getView();
    obstacleVisibleScreen.put(model, model.getView());

    obstacle.setX(screenUtils.getWidthPx());
    obstacle.setY(height);
    viewBackground.addView(obstacle);

    model.animateObstacle();
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();
    subscriptions.add(controller.onTap().subscribe(aVoid -> {
      jump();
    }));
  }

  ValueAnimator va;

  public void jump() {
    if (va != null) {
      va.cancel();
    }
    va = ValueAnimator.ofFloat(bird.getY(), bird.getY() - 100);
    va.setDuration(100);
    va.addUpdateListener(animation -> {
      Float value = (float) animation.getAnimatedValue();
      bird.setY(value);
      float x = bird.getX() + bird.getWidth();
      float y = bird.getY() + bird.getHeight();

      //Timber.e("ok " + x + " " + y);
    });
    va.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        fallBird();
      }
    });
    va.start();
  }

  public void fallBird() {
    if (va != null) {
      va.cancel();
    }
    va = ValueAnimator.ofFloat(bird.getY(), bird.getY() + screenUtils.getHeightPx());
    va.setDuration(1000);
    va.setInterpolator(new AccelerateInterpolator());
    va.addUpdateListener(animation -> {
      Float value = (float) animation.getAnimatedValue();
      bird.setY(value);
      if (bird.getY() > screenUtils.getHeightPx() || bird.getY() < 0) {
        Timber.w("GAME OVER");
      }
    });
    va.start();
  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    Timber.e(" SOEF on start bird Rush");
    wordingPrefix = "game_bird_rush_";
    gameOver = false;
    super.start(game, mapObservable, liveViewsObservable, userId);

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> imReady()));

    subscriptions.add(mapObservable.subscribe(peerMap -> {
      int index = 0;
      for (String key : peerMap.keySet()) {
        Timber.e(" SOEF " + peerMap.get(key) + " " + peerMap.size());
        birdsList.clear();
        ImageView i = new ImageView(context);
        i.setImageDrawable(ContextCompat.getDrawable(context, birdsImage[index]));
        birdsList.put(peerMap.get(key), i);
        index++;
      }
    }));
  }

  @Override public void stop() {
    super.stop();
    Timber.e(" SOEF on stop");
    viewBackground.stop(obstacleVisibleScreen);
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

  private JSONObject getObstaclePayload(List<BirdRushObstacle> list) {
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    for (BirdRushObstacle o : list) {
      array.put(o.asJSON());
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
