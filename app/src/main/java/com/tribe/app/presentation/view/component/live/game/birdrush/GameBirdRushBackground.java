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
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.tribe.app.presentation.view.component.live.game.birdrush.GameBirdRushView.SPEED_BACK_SCROLL;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameBirdRushBackground extends View {
  private static final int speedPx = 10;

  @Inject ScreenUtils screenUtils;
  @Inject User currentUser;

  private static Bitmap splashBtm = null;
  private static Bitmap obstacleBtm = null;
  private static Bitmap birdBtm = null;

  private static int screenWidth;
  private static int screenHeight;
  private Rect dstSplash;
  private Rect dstSplash2;
  private Rect dstObsc;
  private Rect dstBird;

  private int xScroll = 0, yScroll = 0, xFinalBirdPos, yFinalBirdPos, yInitTranslation;
  private boolean pause = false, displayFirstObstacle = false, entranceBirdFinish = false;

  private BirdRushObstacle obstaclePoped = null;
  private Map<BirdRushObstacle, Rect> obstaclePopedList = new HashMap<>();
  private List<BirdRushObstacle> obstaclesList = new ArrayList<>();
  private List<BirdRush> birdList = new ArrayList<>();

  /**
   * OBSERVABLES
   */
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsAnimation = new CompositeSubscription();
  private PublishSubject<Void> onGameOver = PublishSubject.create();

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
    initView();
    initResource();
  }

  private void initView() {
    if (splashBtm == null) {
      splashBtm = BitmapFactory.decodeResource(getResources(), R.drawable.game_birdsrush_sky);
      obstacleBtm = BitmapFactory.decodeResource(getResources(), R.drawable.game_birdrush_obstacle);
      birdBtm = BitmapFactory.decodeResource(getResources(), R.drawable.game_bird1);
    }
  }

  private void initResource() {
    screenWidth = screenUtils.getWidthPx();
    screenHeight = screenUtils.getHeightPx();

    yInitTranslation = screenUtils.dpToPx(200);

    dstSplash =
        new Rect(xScroll, yScroll, xScroll + screenWidth, yScroll + screenUtils.getHeightPx());
    dstSplash2 = new Rect(xScroll + screenWidth, yScroll, xScroll + (2 * screenWidth),
        yScroll + screenUtils.getHeightPx());

    xFinalBirdPos =
        (screenWidth / 2) - (birdBtm.getWidth() / 2) - screenUtils.dpToPx(10); // Middle position

    yFinalBirdPos = (screenHeight / 2) - (birdBtm.getHeight() / 2) - screenUtils.dpToPx(10);

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        SPEED_BACK_SCROLL = (long) (screenWidth * 20);
      }
    });
  }

  @Override protected void onDraw(Canvas canvas) {
    displayBackground(canvas);
    displayObstacles(canvas);
    displayBirds(canvas);
    handleCollision(canvas);
    handleBirdWallCollision();
  }

  private void handleBirdWallCollision() {
    BirdRush myBird = getMyBird();
    boolean crossWall =
        (myBird.getY() < 0) || (myBird.getY() + birdBtm.getHeight()) > screenUtils.getHeightPx();
    if (entranceBirdFinish && crossWall) {
      gameOver();
    }
  }

  private void displayBackground(Canvas canvas) {
    dstSplash.set(xScroll, yScroll, xScroll + screenWidth, yScroll + screenUtils.getHeightPx());
    dstSplash2.set(xScroll + screenWidth, yScroll, xScroll + (2 * screenWidth),
        yScroll + screenUtils.getHeightPx());

    canvas.drawBitmap(splashBtm, null, dstSplash, null);
    canvas.drawBitmap(splashBtm, null, dstSplash2, null);
  }

  private void displayObstacles(Canvas canvas) {
    /**
     *  anim OBSTACLE
     */
    for (Map.Entry<BirdRushObstacle, Rect> entry : obstaclePopedList.entrySet()) {
      BirdRushObstacle b = entry.getKey();
      Rect rect = entry.getValue();
      //Timber.e("SOEF obs Poped Move : " + b.getX() + " " + b.getY() + " " + b.getViewHeight());
      rect.set(b.getX(), b.getY(), b.getX() + BirdRushObstacle.wiewWidth,
          Math.round(b.getY() + b.getViewHeight()));
      canvas.drawBitmap(obstacleBtm, null, rect, null);
    }

    /**
     *  add OBSTACLE
     */
    if (obstaclePoped != null) {
      Timber.e("SOEF T DISPLAY obstPoped " + obstaclePoped.getX() + " " + obstaclePoped.getY());
      dstObsc = new Rect(obstaclePoped.getX(), obstaclePoped.getY(),
          obstaclePoped.getX() + BirdRushObstacle.wiewWidth,
          Math.round(obstaclePoped.getY() + obstaclePoped.getViewHeight()));
      obstaclePopedList.put(obstaclePoped, dstObsc);
      obstaclePoped = null;
      canvas.drawBitmap(obstacleBtm, null, dstObsc, null);
    }
  }

  private void displayBirds(Canvas canvas) {
    /**
     *  Move MY BIRD
     */
    for (BirdRush birdRush : birdList) {
      dstBird.set(birdRush.getX(), birdRush.getY(), birdRush.getX() + birdBtm.getWidth(),
          birdRush.getY() + birdBtm.getHeight());
      canvas.drawBitmap(birdBtm, null, dstBird, null);
    }
  }

  private void handleCollision(Canvas canvas) {
    boolean finish = false;
    BirdRush myBird = getMyBird();

    for (int i = 0; i < obstaclesList.size(); i++) {
      BirdRushObstacle o = obstaclesList.get(i);
      if (o != null && myBird != null && o.getX() > 0 && o.getX() > screenUtils.getWidthPx() / 2) {

        if (o.getX() - o.getWiewWidth() < (screenUtils.getWidthPx() / 2)) {
          if (isBetween(o.getY(), o.getY() + o.getViewHeight(), myBird.getY())) {
            obstacleBtm =
                BitmapFactory.decodeResource(getResources(), R.drawable.game_birdrush_obstacle_red);
            displayObstacles(canvas);
            gameOver();
          }
        }
      }
    }
  }

  private void gameOver() {
    stop();
    onGameOver.onNext(null);
  }

  private void moveBackBackground() {
    xScroll = xScroll - speedPx;
    if (xScroll < -screenWidth) xScroll = 0;
  }

  private void moveObstacleList() {
    for (Map.Entry<BirdRushObstacle, Rect> entry : obstaclePopedList.entrySet()) {
      BirdRushObstacle o = entry.getKey();
      o.setX(o.getX() - speedPx);
      o.setY(o.getY());
    }
  }

  private void startBirds() {
    int v = 15;
    BirdRush myBird = getMyBird();

    if (myBird.getX() < xFinalBirdPos) {              // entrance of the bird
      myBird.setX(myBird.getX() + v);
    } else {
      myBird.setX(xFinalBirdPos);                     // middle of the screen
    }

    int vitesse = (int) (((v * (yInitTranslation)) / (xFinalBirdPos)) * 0.9);

    if (myBird.getY() > yFinalBirdPos) {              // entrance of the bird
      myBird.setY(myBird.getY() - vitesse);
    } else {
      myBird.setY(yFinalBirdPos);
    }

    if (myBird.getX() >= xFinalBirdPos && myBird.getY() >= yFinalBirdPos) {
      entranceBirdFinish = true;
    }
  }

  final static int GRAVITY_SPEED = 7;
  int yBirdDelayPos = GRAVITY_SPEED;

  private void moveBirds() {
    BirdRush myBird = getMyBird();
    myBird.setX(xFinalBirdPos);                     // middle of the screen
    int vitesse = ((yBirdDelayPos * (yInitTranslation)) / (xFinalBirdPos - birdBtm.getWidth()));
    myBird.setY(myBird.getY() + vitesse);            // gravity
  }

  public void jumpBird(BirdRush b) {
    yBirdDelayPos = -GRAVITY_SPEED;
    subscriptions.add(Observable.timer(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          Timber.e("JUMP " + aLong);
          yBirdDelayPos = GRAVITY_SPEED;
        }));
  }

  private void setTimer() {
    Timber.e("SOEF NEW TIMEER RESET");

    subscriptions.add(Observable.interval(0, 16, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          startPlaying();
        }));

    subscriptions.add(Observable.interval(0, 100, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::popObstacles));
  }

  private void startPlaying() {
    if (!pause) {
      if (!entranceBirdFinish) {
        startBirds();
      } else {
        moveBackBackground();
        moveObstacleList();
      }
    }
    if (entranceBirdFinish) moveBirds();

    invalidate();
  }

  Double delay = null;
  int i = 0;

  private void popObstacles(Long aLong) {
    // Timber.e(" ON TOME : " + aLong + " " + obstaclesList.size());
    if (obstaclesList != null && !obstaclesList.isEmpty()) {
      aLong = aLong * 100;
      // init first obstacleBtm
      if (!displayFirstObstacle) {
        displayFirstObstacle = true;
        obstaclePoped = obstaclesList.get(0);
        delay = ((obstaclePoped.getNextSpawn() * 1000) + aLong);
        Timber.e("SOEF TI: " + i + " " + aLong + " " + obstaclesList.size());
        i++;
      }

      // init next obstacleBtm
      if (delay != null && (delay == aLong.doubleValue())) {
        if (i < obstaclesList.size()) {
          obstaclePoped = obstaclesList.get(i);
          delay = ((obstaclePoped.getNextSpawn() * 1000) + aLong);
          i++;
          Timber.e("SOEF T : " + i + " " + aLong + " " + obstaclesList.size());
        }
      }
    }
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

  public boolean haveBird(TribeGuest guest) {
    for (BirdRush birdRush : birdList) {
      if (birdRush.getGuestId().equals(guest.getId())) {
        return true;
      }
    }
    return false;
  }

  private boolean isBetween(float x1, float x2, float pos) {
    return pos > x1 && pos < x2;
  }

  /**
   * PUBLIC
   */

  public BirdRush getMyBird() {
    for (BirdRush b : birdList) {
      if (b.isMine()) return b;
    }
    return null;
  }

  private void initBirdPosition(BirdRush birdRush) {
    int yPos =
        (screenHeight / 2) - (birdBtm.getHeight() / 2) - screenUtils.dpToPx(10) + yInitTranslation;
    int xPos = -birdBtm.getWidth();
    birdRush.setX(xPos);
    birdRush.setY(yPos);
  }

  public void addBird(BirdRush birdRush) {
    initBirdPosition(birdRush);
    dstBird = new Rect(birdRush.getX(), birdRush.getY(), birdRush.getX() + birdBtm.getWidth(),
        birdRush.getY() + birdBtm.getHeight());
    birdList.add(birdRush);
    Timber.d("SOEF BIRD LIST SIZE " + birdList.size());
  }

  public void start() {
    pause = false;
    setTimer();
  }

  public void stop() { // List<BirdRushobstaclesListObstacle>
    pause = true;
    Timber.e("SOEF BACKGROUND  stop ");

    subscriptions.add(Observable.timer(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          clearObstacles();
          invalidate();
        }));
  }

  public void dispose() {
    Timber.e("SOEF BACKGROUND  dispose ");
    resetParams();
    subscriptions.unsubscribe();
    subscriptionsAnimation.unsubscribe();
  }

  public void addObstacles(List<BirdRushObstacle> list) {
    obstaclesList.addAll(list);
  }

  public void clearObstacles() {
    Timber.e("SOEF clear obstacles");
    obstaclesList.clear();
    obstaclePopedList.clear();
  }

  public void resetParams() {
    Timber.e("SOEF resetParams");
    obstacleBtm = BitmapFactory.decodeResource(getResources(), R.drawable.game_birdrush_obstacle);
    clearObstacles();
    entranceBirdFinish = false;
    displayFirstObstacle = false;
    subscriptions.clear();

    for (BirdRush b : birdList) {
      initBirdPosition(b);
    }
  }

  /**
   * OBSERVABLES
   */

  public Observable<Void> onGameOver() {
    return onGameOver;
  }
}
