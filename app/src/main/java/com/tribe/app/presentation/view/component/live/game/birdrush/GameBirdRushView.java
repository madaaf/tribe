package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
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

  private BirdController controller;
  private Double delay = null;
  private boolean gameOver = false;
  private Map<TribeGuest, ImageView> birdsList = new HashMap<>();
  private boolean startedAsSingle = false, didRestartWhenReady = false;
  public static Long SPEED_BACK_SCROLL;

  private Integer[] birdsImage = new Integer[] {
      R.drawable.game_bird1, R.drawable.game_bird2, R.drawable.game_bird3, R.drawable.game_bird4,
      R.drawable.game_bird5, R.drawable.game_bird6, R.drawable.game_bird7, R.drawable.game_bird8
  };

  // OBSERVABLES
  protected CompositeSubscription subscriptions;
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
        SPEED_BACK_SCROLL = Long.valueOf(screenUtils.getWidthPx() / 120);
      }
    });
  }

  @Override protected GameEngine generateEngine() {
    return new GameBirdRushEngine(context, GameBirdRushEngine.Level.MEDIUM);
  }

  @Override protected int getSoundtrack() {
    return SoundManager.ALIENS_ATTACK_SOUNDTRACK;
  }

  List<BirdRushObstacle> obstaclesList = new ArrayList<>();

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
                  setEngineFromUser(obstaclesList);
                }
                if (actionKey.equals(BIRD_ACTION_ADD_OBSTACLE)) {
                  JSONArray jsonObstacles = message.getJSONArray(BIRD_KEY_OBSTACLE);
                  List<BirdRushObstacle> obstacles = transform(jsonObstacles);
                  obstaclesList.addAll(obstacles);
                  if (obstaclesList != null && !obstacles.isEmpty()) {
                    setEngineFromUser(obstaclesList);
                  }
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

  Subscription timer;
  int i = 0;

  private void setEngineFromUser(List<BirdRushObstacle> obstaclesList) {
    GameBirdRushEngine engine = new GameBirdRushEngine(context, GameBirdRushEngine.Level.MEDIUM);
    //subscriptions.add();
    if (timer == null) {
      timer = Observable.interval(3000, 100, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            if (!obstaclesList.isEmpty()) {
              //if (gameOver) return;
              aLong = aLong * 100;
              // init first obstacle
              if (aLong == 0L) {
                obs = obstaclesList.get(0);
                delay = ((obs.getNextSpawn() * 1000) + aLong);
                Timber.e("SOEF ANIMATE OBSC "
                    + aLong
                    + " "
                    + i
                    + " "
                    + obs.toString()
                    + obstaclesList.size());
                v = animateObstacle(obs);
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
                v = animateObstacle(obs);
              }

              handleCollisionWithObstacle();
            }
          });
      subscriptions.add(timer);
    }
  }

  private void setTimer() {
    GameBirdRushEngine engine = new GameBirdRushEngine(context, GameBirdRushEngine.Level.MEDIUM);
    subscriptions.add(Observable.interval(3000, 100, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          /*
          if (gameOver) return;
          aLong = aLong * 100;
          // init first obstacle
          if (aLong == 0L) {
            obs = engine.generateObstacle();
            delay = (obs.getNextSpawn() + aLong);
            v = animateObstacle(obs);
          }

          // init next obstacle
          if (delay != null && (delay == aLong.doubleValue())) {
            obs = engine.generateObstacle();
            delay = (obs.getNextSpawn()) + aLong;
            v = animateObstacle(obs);
          }
          */
          handleCollisionWithObstacle();
        }));
  }

  protected void setupGameLocally(String userId, Set<String> players, long timestamp) { // SOEF
    super.setupGameLocally(userId, players, timestamp);
    Timber.d("SET UP LOCALLY " + userId + " " + players.size() + " " + timestamp);
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
    Timber.d("Game Bird Rush Over : " + winnerId);
    super.gameOver(winnerId, isLocal);
    viewBirds.removeAllViews();
  }

  private void overcomeObstacle() {
    if (!pending) {
      addPoints(1, currentUser.getId(), true);
    }
  }

  private void animateBird() {

  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();
    Timber.d("start master engine bird rush ");
/* // SOEF
    subscriptionsSession.add(
        ((GameAliensAttackEngine) gameEngine).onAlien().subscribe(alienView -> {
          webRTCRoom.sendToPeers(getAlienPayload(alienView.asJSON()), true);
          animateAlien(alienView);
        }));
*/

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

  // SOEF
  ImageView v;
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
    gameOver = true;
    setOnTouchListener(null);
    fallBird();
    // obstacle.getAnimation().cancel();
    obstacle.clearAnimation();
    obstacle.setImageDrawable(
        ContextCompat.getDrawable(context, R.drawable.game_birdrush_obstacle_red));
  /*
    animator.cancel();
    for (Map.Entry<BirdRushObstacle, ImageView> entry : obstacleVisibleScreen.entrySet()) {
      ImageView obsclView = entry.getValue();
      obsclView.animate().cancel();
    }

    backgroundOne.clearAnimation();
    backgroundTwo.clearAnimation();
    */

    resetScores(true);
    iLost();
  }

  private boolean isBetween(float x1, float x2, float pos) {
    if (pos > x1 && pos < x2) {
      return true;
    }
    return false;
  }

  private ImageView animateObstacle(BirdRushObstacle model) {
    ImageView obstacle = new ImageView(context);
    obstacleVisibleScreen.put(model, obstacle);
    obstacle.setScaleType(ImageView.ScaleType.FIT_XY);
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.END;
    obstacle.setImageDrawable(
        ContextCompat.getDrawable(context, R.drawable.game_birdrush_obstacle));
    obstacle.setLayoutParams(params);
    int height = Math.round(model.getHeight() * screenUtils.getHeightPx());
    int width = 75;
    obstacle.getLayoutParams().height = height;
    obstacle.getLayoutParams().width = width;

    obstacle.setX(screenUtils.getWidthPx());
    obstacle.setY(model.getStart() * screenUtils.getHeightPx() - height / 2);

    addView(obstacle);
    obstacle.animate()
        .translationX(-screenUtils.getWidthPx() - 2 * width)
        .setDuration(SPEED_BACK_SCROLL)
        .start();

    // TRANSLATION
    if (model.getTranslation() != null) {
      ValueAnimator trans =
          ValueAnimator.ofFloat(obstacle.getY(), obstacle.getY() - model.getTranslation().getY(),
              obstacle.getY() + model.getTranslation().getY());
      trans.setDuration(1000);
      trans.addUpdateListener(animation -> {
        Float value = (float) animation.getAnimatedValue();
        obstacle.setY(value);
      });
      trans.setRepeatCount(Animation.INFINITE);
      trans.setRepeatMode(ValueAnimator.REVERSE);
      trans.start();
    }

    // ROTATION
    if (model.getRotation() != null) {
      ValueAnimator rotation = ValueAnimator.ofFloat(0f, -model.getRotation().getAngle(), 0f,
          model.getRotation().getAngle());
      rotation.setDuration(5000);
      rotation.addUpdateListener(animation -> {
        Float value = (float) animation.getAnimatedValue();
        obstacle.setPivotX(width / 2);
        obstacle.setPivotY(height / 2);
        obstacle.setRotation(value);
      });
      rotation.setInterpolator(new LinearInterpolator());
      rotation.setRepeatCount(Animation.INFINITE);
      rotation.setRepeatMode(ValueAnimator.REVERSE);
      rotation.start();
    }

    return obstacle;
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

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void addBird() {

  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    Timber.d(" on start bird Rush");
    wordingPrefix = "game_bird_rush_";
    gameOver = false;
    super.start(game, mapObservable, liveViewsObservable, userId);
    viewBackground.start();

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
    viewBackground.stop();
  }

  @Override public void dispose() {
    super.dispose();
    viewBackground.dispose();
    viewBirds.removeAllViews();
    subscriptions.unsubscribe();
  }

  @Override public void setNextGame() {
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

}
