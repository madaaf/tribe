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
import com.tribe.app.domain.entity.User;
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
  @Inject User currentUser;

  private static Bitmap splash = null;
  private static Bitmap obstacle = null;

  private static int screenWidth;
  private Rect dstSplash;
  private Rect dstSplash2;
  private Rect dstObsc;

  private int x = 0;
  private int y = 0;
  private boolean pause = false, displayFirstObstacle = false;

  private BirdRushObstacle obstaclePoped = null;
  private Map<BirdRushObstacle, ImageView> obstacleVisibleScreen = new HashMap<>();
  private Map<BirdRushObstacle, Rect> obstaclePopedList = new HashMap<>();
  private List<BirdRushObstacle> obstaclesList = new ArrayList<>();
  private List<Bitmap> obstaclesBitmap = new ArrayList<>();

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

    // anim old one
    for (Map.Entry<BirdRushObstacle, Rect> entry : obstaclePopedList.entrySet()) {
      BirdRushObstacle b = entry.getKey();
      Rect rect = entry.getValue();
      Timber.e(
          "DISPLAY obstaclePoped Move : " + b.getX() + " " + b.getY() + " " + b.getViewHeight());
      rect.set(b.getX(), b.getY(), b.getX() + BirdRushObstacle.wiewWidth, Math.round(b.getY() + b.getViewHeight()));
      canvas.drawBitmap(obstacle, null, rect, null);
    }

    // add new Obstacle
    if (obstaclePoped != null) {
      Timber.e("DISPLAY obstaclePoped " + obstaclePoped.getX() + " " + obstaclePoped.getY());
      dstObsc = new Rect(obstaclePoped.getX(), obstaclePoped.getY(), obstaclePoped.getX() + BirdRushObstacle.wiewWidth,
          Math.round(obstaclePoped.getY() + obstaclePoped.getViewHeight()));
      obstaclePopedList.put(obstaclePoped, dstObsc);
      obstaclePoped = null;
      canvas.drawBitmap(obstacle, null, dstObsc, null);
    }
  }

  private void animateObstacle(BirdRushObstacle model) {
   /* float height = (model.getStart() * screenUtils.getHeightPx() - model.getView().getHeight() / 2);
    ImageView obstacle = model.getView();
    obstacleVisibleScreen.put(model, model.getView());

    obstacle.setX(screenUtils.getWidthPx());
    obstacle.setY(height);

    obstaclesBitmap.add(bmap);*/

    //model.animateObstacle();
  }

  private void initView() {
    positionBird();
    if (splash == null) {
      splash = BitmapFactory.decodeResource(getResources(), R.drawable.game_birdsrush_sky);
      obstacle = BitmapFactory.decodeResource(getResources(), R.drawable.game_birdrush_obstacle);
    }
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
  }

  public void addObstacle(BirdRushObstacle obstacle) {
    obstaclesList.add(obstacle);
  }

  public void addObstacles(List<BirdRushObstacle> list) {
    obstaclesList.addAll(list);
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
  }



  private boolean isBetween(float x1, float x2, float pos) {
    if (pos > x1 && pos < x2) {
      return true;
    }
    return false;
  }


  */

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

  public void startPlaying() {
    if (!pause) {
      setBackScroll();
      moveObstacleList();
      invalidate();
    }
  }

  private void setBackScroll() {
    x = x - 2;
    if (x < -screenWidth) x = 0;
  }

  private void moveObstacleList() {
    for (Map.Entry<BirdRushObstacle, Rect> entry : obstaclePopedList.entrySet()) {
      BirdRushObstacle o = entry.getKey();
      o.setX(o.getX() - 2);
      o.setY(o.getY());
    }
  }

  private void setTimer() {
    Timber.e("SOEF NEW TIMEER RESET");

    subscriptions.add(Observable.interval(3000, 16, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          startPlaying();
        }));

    subscriptions.add(Observable.interval(3000, 100, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          // handleCollisionWithObstacle();
          popObstacles(aLong);
        }));
  }

  Double delay = null;
  int i = 0;

  private void popObstacles(Long aLong) {
    // Timber.e(" ON TOME : " + aLong + " " + obstaclesList.size());
    if (obstaclesList != null && !obstaclesList.isEmpty()) {
      //if (gameOver) return;
      aLong = aLong * 100;
      // init first obstacle
      if (!displayFirstObstacle) {
        displayFirstObstacle = true;
        obstaclePoped = obstaclesList.get(0);
        delay = ((obstaclePoped.getNextSpawn() * 1000) + aLong);
        Timber.e("SOEF " + aLong + " " + i + " " + obstaclePoped.toString() + obstaclesList.size());
        animateObstacle(obstaclePoped);
        i++;
      }

      // init next obstacle
      if (delay != null && (delay == aLong.doubleValue())) {
        if (i < obstaclesList.size()) {
          i++;
          obstaclePoped = obstaclesList.get(i);
          delay = ((obstaclePoped.getNextSpawn() * 1000) + aLong);
          Timber.e("SOEF " + " " + i + " " + obstaclePoped.toString() + " " + obstaclesList.size());
          animateObstacle(obstaclePoped);
        }
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
