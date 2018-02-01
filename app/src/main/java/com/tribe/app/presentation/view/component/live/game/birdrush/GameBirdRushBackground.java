package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
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

/**
 * Created by tiago on 10/31/2017.
 */

public class GameBirdRushBackground extends View {
  private static final int speedPx = 7;

  @Inject ScreenUtils screenUtils;
  @Inject User currentUser;

  private static Bitmap splashBtm = null;
  private static Bitmap obstacleBtm = null;
  private static Bitmap birdBtm = null;
  private static Bitmap nameLabelBtm = null;

  private static int screenWidth;
  private static int screenHeight;
  private Rect dstSplash;
  private Rect dstSplash2;
  private Rect dstObsc;
  private Rect dstBird;
  private Rect dstnameLabelBird;

  private int xScroll = 0, yScroll = 0, xCenterBirdPos, yCenterBirdPos, yInitTranslation;
  private boolean pause = false, displayFirstObstacle = false, entranceBirdFinish = false;

  private BirdRushObstacle obstaclePoped = null;
  private Map<BirdRushObstacle, Rect> obstaclePopedList = new HashMap<>();
  private List<BirdRushObstacle> obstaclesList = new ArrayList<>();
  private List<BirdRush> birdList = new ArrayList<>();
  private List<String> crossObstacle = new ArrayList<>();

  /**
   * OBSERVABLES
   */
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsAnimation = new CompositeSubscription();
  private PublishSubject<Void> onGameOver = PublishSubject.create();
  private PublishSubject<Void> onAddPoint = PublishSubject.create();

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
      nameLabelBtm = BitmapFactory.decodeResource(getResources(), R.drawable.game_bird_bck);
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

    xCenterBirdPos = (screenWidth / 2) - (birdBtm.getWidth() / 2) - screenUtils.dpToPx(10);
    yCenterBirdPos = (screenHeight / 2) - (birdBtm.getHeight() / 2) - screenUtils.dpToPx(10);
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

      rect.set(b.getX(), b.getY(), b.getX() + BirdRushObstacle.wiewWidth,
          Math.round(b.getY() + b.getBirdHeight()));
      canvas.drawBitmap(obstacleBtm, null, rect, null);

      if (b.getX() < screenUtils.getWidthPx() / 2) {
        if (!crossObstacle.contains(b.getBirdId())) {
          onAddPoint.onNext(null);
          crossObstacle.add(b.getBirdId());
        }
      }
    }

    /**
     *  add OBSTACLE
     */
    if (obstaclePoped != null) {
      Timber.e("SOEF T DISPLAY obstPoped " + obstaclePoped.getX() + " " + obstaclePoped.getY());
      dstObsc = new Rect(obstaclePoped.getX(), obstaclePoped.getY(),
          obstaclePoped.getX() + BirdRushObstacle.wiewWidth,
          Math.round(obstaclePoped.getY() + obstaclePoped.getBirdHeight()));
      obstaclePopedList.put(obstaclePoped, dstObsc);
      obstaclePoped = null;

      Matrix rotator = new Matrix();
      // rotate around (0,0)
      rotator.postRotate(90);
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

      // draw text to the Canvas center
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setColor(Color.WHITE);
      paint.setTextSize(25);
      paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
      Rect bounds = new Rect();
      String text = birdRush.getName();
      paint.getTextBounds(text, 0, text.length(), bounds);

      int txtWidth = bounds.width();

      int nameYPos = birdRush.getY() + birdBtm.getHeight() + screenUtils.dpToPx(0);
      int nameXPos = birdRush.getX() + (birdBtm.getWidth() / 2) - (txtWidth / 2);

      int margin = screenUtils.dpToPx(15);

      dstnameLabelBird.set(nameXPos - margin, nameYPos, nameXPos + txtWidth + margin,
          nameYPos + screenUtils.dpToPx(30));

      canvas.drawBitmap(birdRush.getBitmap(), null, dstBird, null);
      canvas.drawBitmap(birdRush.getBackgroundBitmap(), null, dstnameLabelBird, null);
      canvas.drawText(text, nameXPos, nameYPos + screenUtils.dpToPx(18), paint);
    }
  }

  private void handleCollision(Canvas canvas) {
    BirdRush myBird = getMyBird();

    for (int i = 0; i < obstaclesList.size(); i++) {
      BirdRushObstacle o = obstaclesList.get(i);
      if (o != null && myBird != null && o.getX() > 0 && o.getX() > screenUtils.getWidthPx() / 2) {

        if (o.getX() - o.getBirdWidth() < (screenUtils.getWidthPx() / 2)) {
          if (isBetween(o.getY(), o.getY() + o.getBirdHeight(), myBird.getY())) {
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
   /* stop();
    onGameOver.onNext(null);*/
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

    Handler handler1 = new Handler();
    for (int i = 0; i < birdList.size(); i++) {
      int finalI = i;
      handler1.postDelayed(() -> {
        Timber.e("ANIMD BIRD INDEX " + finalI);

        BirdRush b = birdList.get(finalI);

        if (b.getX() < xCenterBirdPos) {              // entrance of the bird
          b.setX(b.getX() + v);
        } else {
          b.setX(xCenterBirdPos);                     // middle of the screen
        }
        int coefIndex = (birdList.size() - finalI) / birdList.size();
        int vitesse = (v * (yInitTranslation)) / (xCenterBirdPos) + coefIndex;

        if (b.getY() > yCenterBirdPos) {              // entrance of the bird
          b.setY(b.getY() - vitesse);
        } else {
          b.setY(yCenterBirdPos);
        }

        if (b.getX() >= xCenterBirdPos
            && b.getY() >= yCenterBirdPos
            && finalI == birdList.size() - 1) {
          entranceBirdFinish = true;
        }
      }, 300 * i);
    }
  }

  private void moveBirds() {
    for (int i = 0; i < birdList.size(); i++) {
      BirdRush bird = birdList.get(i);
      bird.setX(xCenterBirdPos);

      if (bird.getSpeedY() < 0) {
        bird.setSpeedY(
            bird.getSpeedY() * 2 / 3 + getSpeedTimeDecrease() / 5);  // The character is moving up
        Timber.e(" SOEF Y MOVING UP " + bird.getSpeedY());
      } else {
        bird.setSpeedY(bird.getSpeedY() + getSpeedTimeDecrease());  // the character is moving down
        Timber.d(" SOEF Y MOVING DOWN " + bird.getSpeedY());
      }

      if (bird.getSpeedY() > getMaxSpeed()) {
        bird.setSpeedY(getMaxSpeed());  // speed limit
        Timber.w(" SOEF Y LIMIT " + bird.getSpeedY());
      }

      bird.setY(Math.round(bird.getY() + bird.getSpeedY()));
    }
  }

  public void jumpBird(String guestId) {
    BirdRush b = getBird(guestId);
    Timber.i(" SOEF Y JUMP " + b.getSpeedY());
    b.setSpeedY(getTabSpeed());
    b.setY(b.getY() + getPosTabIncrease());
  }

  protected float getSpeedTimeDecrease() {
    float speed = screenUtils.getHeightPx() / 640;
    return speed;
  }

  protected float getMaxSpeed() {
    float speed = screenUtils.getHeightPx() / 71.2f;
    return speed;
  }

  protected float getTabSpeed() {
    float speed = -screenUtils.getHeightPx() / 16f;
    return speed;
  }

  protected int getPosTabIncrease() {
    int speed = -screenUtils.getHeightPx() / 100;
    return speed;
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

  public BirdRush getBird(String guestId) {
    for (BirdRush b : birdList) {
      if (b.getGuestId().equals(guestId)) return b;
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

  public void addBird(BirdRush birdRush, int index) {
    birdRush.setBitmap(getResources());
    initBirdPosition(birdRush);

    dstBird = new Rect(birdRush.getX(), birdRush.getY(), birdRush.getX() + birdBtm.getWidth(),
        birdRush.getY() + birdBtm.getHeight());

    dstnameLabelBird = new Rect(birdRush.getX(), birdRush.getY() + birdBtm.getHeight(),
        birdRush.getX() + birdBtm.getWidth(), birdRush.getY() + birdBtm.getHeight());

    birdList.add(birdRush);
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
    crossObstacle.clear();

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

  public Observable<Void> onAddPoint() {
    return onAddPoint;
  }
}
