package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.tribe.app.presentation.view.component.live.game.birdrush.GameBirdRushView.SPEED_BACK_SCROLL;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameBirdRushBackground extends View {

  @Inject ScreenUtils screenUtils;

  private static Bitmap splash = null;
  private static int screenWidth;
  private Rect dstSplash;
  private Rect dstSplash2;

  private int x = 0;
  private int y = 0;
  private boolean pause = false, displayFirstObstacle = false;

  private Map<BirdRushObstacle, ImageView> obstacleVisibleScreen = new HashMap<>();
  private List<BirdRushObstacle> obstaclesList = new ArrayList<>();

  /**
   * OBSERVABLES
   */
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsAnimation = new CompositeSubscription();
  private Subscription timer;

  public GameBirdRushBackground(@NonNull Context context) {
    super(context);
    init();
  }

  public GameBirdRushBackground(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    initDependencyInjector();
    initResource();
    initView();
    setTimer();
  }

  private void initResource() {
    screenWidth = screenUtils.getWidthPx();
    dstSplash = new Rect(x, y, x + screenWidth, y + screenUtils.getHeightPx());
    dstSplash2 = new Rect(x + screenWidth, y, x + (2 * screenWidth), y + screenUtils.getHeightPx());

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        SPEED_BACK_SCROLL = (long) (screenWidth * 20);
      }
    });
  }

  @Override protected void onDraw(Canvas canvas) {
    dstSplash.set(x, y, x + screenWidth, y + screenUtils.getHeightPx());
    dstSplash2.set(x + screenWidth, y, x + (2 * screenWidth), y + screenUtils.getHeightPx());

    canvas.drawBitmap(splash, null, dstSplash, null);
    canvas.drawBitmap(splash, null, dstSplash2, null);
  }

  private void initView() {
    positionBird();
    if (splash == null) {
      splash = BitmapFactory.decodeResource(getResources(), R.drawable.game_birdsrush_sky);
    }
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
  }

  public void addObstacle(BirdRushObstacle obstacle) {
    obstaclesList.add(obstacle);
  }

  public void addObstacles(List<BirdRushObstacle> obstaclesList) {
    obstaclesList.addAll(obstaclesList);
  }

  public void clearObstacles() {
    obstaclesList.clear();
  }

  /**
   * PUBLIC
   */

  public void start() {
    pause = false;
  }

  /*
  private void handleCollisionWithObstacle() {
    if (myBird == null) {
      return;
    }
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
                myBird.getY())) {
              gameOver(obsclView);
            }
          }
        }
      }
    }
  }*/

  public void stop() { // List<BirdRushobstaclesListObstacle>
    pause = true;
    Timber.e("SOEF BACKGROUND  stop ");
    resetTimer();
    // if (animator != null) animator.cancel();
    /*
    for (int i = 0; i < obstaclesList.size(); i++) {
      View v = obstaclesList.get(i).getView();
      //removeView(v);
    }
    */
  }

  public void dispose() {
    Timber.e("SOEF BACKGROUND  dispose ");
    subscriptions.unsubscribe();
    subscriptionsAnimation.unsubscribe();
  }

  public void removeObstacles() {
    /*
    for (int i = 0; i < getChildCount(); i++) {
      View v = getChildAt(i);
      Timber.e("REMOVE VIEW " + v.getId());
      if (v.getTag() != null && v.getTag()
          .toString()
          .startsWith(BirdRushObstacle.BIRD_OBSTACLE_TAG)) {
        removeView(v);
      }
     /* if (v.getId() != R.id.background_one && v.getId() != R.id.background_two) {

      }*/

  }

  public void setBackScrolling() {
    if (!pause) {
      x = x - 2;
      if (x < -screenWidth) x = 0;
      invalidate();
    }
  }

  private void animateObstacle(BirdRushObstacle model) {
    float height = (model.getStart() * screenUtils.getHeightPx() - model.getView().getHeight() / 2);
    ImageView obstacle = model.getView();
    obstacleVisibleScreen.put(model, model.getView());

    obstacle.setX(screenUtils.getWidthPx());
    obstacle.setY(height);

    model.animateObstacle();
  }

  private void setTimer() {
    if (timer == null) {
      Timber.e("SOEF NEW TIMEER RESET");
      subscriptions.add(timer = Observable.interval(3000, 16, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            // handleCollisionWithObstacle();
            popObstacles(aLong);
            setBackScrolling();
          }));
    }
  }

  Double delay = null;
  int i = 0;
  private BirdRushObstacle obs = null;

  private void popObstacles(Long aLong) {
    // Timber.e(" ON TOME : " + aLong + " " + obstaclesList.size());
    if (obstaclesList != null && !obstaclesList.isEmpty()) {
      //if (gameOver) return;
      aLong = aLong * 100;
      // init first obstacle
      if (!displayFirstObstacle) {
        displayFirstObstacle = true;
        obs = obstaclesList.get(0);
        delay = ((obs.getNextSpawn() * 1000) + aLong);
        Timber.e("SOEF " + aLong + " " + i + " " + obs.toString() + obstaclesList.size());
        animateObstacle(obs);
        i++;
      }

      // init next obstacle
      if (delay != null && (delay == aLong.doubleValue())) {
        i++;
        obs = obstaclesList.get(i);
        delay = ((obs.getNextSpawn() * 1000) + aLong);
        Timber.e("SOEF  " + aLong + " " + i + " " + obs.toString() + " " + obstaclesList.size());
        animateObstacle(obs);
      }

      // handleCollisionWithObstacle();
    }
  }

  private void resetTimer() {
    Timber.e("SOEF RESET TIMER");
    displayFirstObstacle = false;
    if (timer != null) timer.unsubscribe();
    timer = null;
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

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void positionBird() {

  }
}
