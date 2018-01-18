package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithEngine;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameBirdRushView extends GameViewWithEngine {

  private static final Long SPEED_BACK_SCROLL = 5000L;
  private boolean startedAsSingle = false, didRestartWhenReady = false;

  @BindView(R.id.background_one) ImageView backgroundOne;
  @BindView(R.id.background_two) ImageView backgroundTwo;
  @BindView(R.id.bird) ImageView bird;
  @BindView(R.id.gameOver) TextView gameOver;

  @Inject ScreenUtils screenUtils;

  private ValueAnimator animator;
  private BirdController controller;
  private Double delay = null;

  private Drawable[] birds = new Drawable[] {
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird1),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird2),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird3),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird4),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird5),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird6),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird7),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird8)
  };

  // OBSERVABLES
  protected CompositeSubscription subscriptions;
  private Map<BirdRushObstacle, ImageView> obstacleVisibleScreen = new HashMap<>();
  private Map<BirdRushObstacle, ImageView> obstacleVisibleScreenBis = new HashMap<>();

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
    setBackScrolling();
    controller = new BirdController(context);

    initSubscriptions();
    setTimer();
    fallBird();
    setOnTouchListener(controller);
  }

  BirdRushObstacle obs = null;

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    Timber.e("ok on DRAw");
  }

  ImageView v;

  private void setTimer() {
    GameBirdRushEngine ok = new GameBirdRushEngine(context, GameBirdRushEngine.Level.MEDIUM);
    subscriptions.add(Observable.interval(3000, 100, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          aLong = aLong * 100;
          // init first obstacle
          if (aLong == 0L) {
            obs = ok.generateObstacle();
            delay = (obs.getNextSpawnDelay() + aLong);
            v = animateObstacle(obs);
          }

          if (v != null) {
            Timber.w("OBSERVALE ok : " + v.getX() + "  " + v.getY());
          }
/*
          // init next obstacle
          if (delay != null && (delay == aLong.doubleValue())) {
            obs = ok.generateObstacle();
            delay = (obs.getNextSpawnDelay()) + aLong;
            animateObstacle(obs);
          }
*/
          displayMovingObstacle(v, obs);
        }));
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
  }

  private void displayMovingObstacle(ImageView obsclView, BirdRushObstacle b) {
    if (obstacleVisibleScreen != null && !obstacleVisibleScreen.isEmpty()) {
      for (Map.Entry<BirdRushObstacle, ImageView> entry : obstacleVisibleScreen.entrySet()) {

      }
      /*value.getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi") @Override public void onGlobalLayout() {
              Timber.w("OBSERVALE " + value.getX() + "  " + value.getY());
            }
          });

      BirdRushObstacle b = entry.getKey();
      ImageView i = entry.getValue();
      */
    }

    if (obsclView != null
        && b != null
        && obsclView.getX() > 0
        && obsclView.getX() > screenUtils.getWidthPx() / 2) {
      if (obsclView.getX() - obsclView.getWidth() < (screenUtils.getWidthPx() / 2)) {
        if (isBetween(obsclView.getY(), obsclView.getY() + obsclView.getHeight(), bird.getY())) {
          gameOver(obsclView);
        }
      }
    }
    Timber.w("OBSERVALE ok1 : "
        + b.getId()
        + " "
        + bird.getY()
        + "  "
        + (bird.getY() + bird.getHeight())
        + " "
        + obsclView.getY()
        + obstacleVisibleScreen.size());
  }

  private void gameOver(ImageView obstacle) {
    setOnTouchListener(null);
    gameOver.setVisibility(VISIBLE);
    fallBird();
    // obstacle.getAnimation().cancel();
    obstacle.clearAnimation();
    obstacle.setImageDrawable(
        ContextCompat.getDrawable(context, R.drawable.game_birdrush_obstacle_red));

    animator.cancel();
    obstacle.animate().cancel();
    backgroundOne.clearAnimation();
    backgroundTwo.clearAnimation();
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
    obstacleVisibleScreenBis = obstacleVisibleScreen;
    obstacle.setScaleType(ImageView.ScaleType.FIT_XY);
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.END;
    obstacle.setImageDrawable(
        ContextCompat.getDrawable(context, R.drawable.game_birdrush_obstacle));
    obstacle.setLayoutParams(params);
    int height = Math.round(model.getHeightRatio() * screenUtils.getHeightPx());
    int width = 75;
    obstacle.getLayoutParams().height = height;
    obstacle.getLayoutParams().width = width;

    addView(obstacle);
    obstacle.setTranslationX(obstacle.getWidth());
    obstacle.setY(model.getStart() * screenUtils.getHeightPx() - height / 2);
    obstacle.animate()
        .translationX(-screenUtils.getWidthPx() - 2 * width)
        .setDuration(SPEED_BACK_SCROLL)
        .start();

    // TRANSLATION
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

    // ROTATION
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
    return obstacle;
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();
    Timber.e("ON ");
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

  private void setBackScrolling() {
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(SPEED_BACK_SCROLL);
        animator.addUpdateListener(animation -> {
          final float progress = (float) animation.getAnimatedValue();
          final float width = backgroundOne.getWidth();
          final float translationX = -width * progress;
          backgroundOne.setTranslationX(translationX);
          backgroundTwo.setTranslationX(translationX + width);
        });

        animator.addListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationRepeat(Animator animation) {
            super.onAnimationRepeat(animation);
          }
        });
        animator.start();
      }
    });
  }

  @Override protected GameEngine generateEngine() {
    // return new GameBirdRushEngine(context, GameBirdRushEngine.Level.MEDIUM);
    return null;
  }

  @Override protected int getSoundtrack() {
    return SoundManager.ALIENS_ATTACK_SOUNDTRACK;
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();
  }

  @Override protected void gameOver(String winnerId, boolean isLocal) {
    Timber.d("Game over : " + winnerId);
    super.gameOver(winnerId, isLocal);
  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    wordingPrefix = "game_aliens_attack_";
  }

  @Override public void stop() {
    super.stop();
  }

  @Override public void dispose() {
    Timber.e(" DISPOSE");
    subscriptions.unsubscribe();
    super.dispose();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */

}
