package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameBirdRushBackground extends View {
  private static final int speedPx = 4;

  @Inject ScreenUtils screenUtils;
  @Inject User currentUser;

  private static Bitmap splashBtm = null, obstacleBtm = null, obstacleRedBtm = null, birdBtm = null;

  private static int screenWidth, screenHeight;
  private Rect dstSplash, dstSplash2, dstnameLabelBird;
  private Paint myBirdPaint;

  private int xScroll = 0, yScroll = 0, xCenterBirdPos, yCenterBirdPos, yInitTranslation;
  private boolean pause = false, displayFirstObstacle = false, entranceBirdFinish = false;

  private BirdRushObstacle obstaclePoped = null;
  private Map<BirdRushObstacle, Rect> obstaclePopedList = new HashMap<>();
  private List<BirdRushObstacle> obstaclesList = new ArrayList<>();
  private Map<BirdRush, Rect> birdList = new HashMap<>();
  private List<String> crossObstacle = new ArrayList<>();

  /**
   * OBSERVABLES
   */
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsAnimation = new CompositeSubscription();
  private PublishSubject<Void> onGameOver = PublishSubject.create();
  private PublishSubject<Void> onAddPoint = PublishSubject.create();
  private Subscription scrollTimer = null, engineTimer = null, killBirdTimer = null;

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
    splashBtm = BitmapFactory.decodeResource(getResources(), R.drawable.game_birdsrush_sky);
    obstacleBtm = BitmapFactory.decodeResource(getResources(), R.drawable.game_birdrush_obstacle);
    birdBtm = BitmapFactory.decodeResource(getResources(), R.drawable.game_bird1);
    obstacleRedBtm =
        BitmapFactory.decodeResource(getResources(), R.drawable.game_birdrush_obstacle_red);
    myBirdPaint = new Paint();
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

  private int updateRotationBird(BirdRush b) {
    int currentAngle = b.getRotation();
    b.setRotation(currentAngle + 5);
    return currentAngle;
  }

  private float updateAngle(BirdRushObstacle b) {
    float angle = b.getBirdRotation().getAngle();
    float currentAngle = b.getBirdRotation().getCurrentRotation();

    if (!(currentAngle <= angle && currentAngle >= -angle)) {
      b.getBirdRotation().setRotationSens(b.getBirdRotation().getRotationSens() * -1);
    }

    b.getBirdRotation().setCurrentRotation(currentAngle + b.getBirdRotation().getRotationSens());

    return currentAngle;
  }

  private int yTranslation(BirdRushObstacle b) {
    if (b.getBirdTranslation() == null) {
      return 0;
    }
    float YMax = b.getBirdTranslation().getY();
    float currentTran = b.getBirdTranslation().getCurrentTransflation();

    if (!(currentTran < YMax && currentTran > -YMax)) {
      b.getBirdTranslation().setCoef(b.getBirdTranslation().getCoef() * -1);
    }
    b.getBirdTranslation().setCurrentTransflation(currentTran + b.getBirdTranslation().getCoef());
    return b.getBirdTranslation().getCoef();
  }

  private void drawObstacleBitmap(Canvas canvas, Rect rect, BirdRushObstacle b) {
    if (b.isHit()) {
      canvas.drawBitmap(obstacleRedBtm, null, rect, null);
    } else {
      canvas.drawBitmap(obstacleBtm, null, rect, null);
    }
  }

  @SuppressLint("WrongConstant") private void displayObstacles(Canvas canvas) {
    /**
     *  anim OBSTACLE
     */
    for (Map.Entry<BirdRushObstacle, Rect> entry : obstaclePopedList.entrySet()) {
      BirdRushObstacle b = entry.getKey();
      Rect rect = entry.getValue();

      rect.set(b.getX(), b.getY(), b.getX() + BirdRushObstacle.wiewWidth,
          Math.round(b.getY() + b.getViewHeight()));

      if (b.getBirdRotation() != null) {
        int pivotX = b.getX() + (BirdRushObstacle.wiewWidth / 2);
        int pivotY = b.getY() + (b.getViewHeight() / 2);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(updateAngle(b), pivotX, pivotY);
        drawObstacleBitmap(canvas, rect, b);
        canvas.restore();
      } else {
        drawObstacleBitmap(canvas, rect, b);
      }

      if (b.getX() < screenUtils.getWidthPx() / 2) {
        if (!crossObstacle.contains(b.getId())) {
          onAddPoint.onNext(null);
          crossObstacle.add(b.getId());
        }
      }
    }

    /**
     *  add OBSTACLE
     */
    if (obstaclePoped != null) {
      obstaclePoped.setIndex(index);
      index++;
      Timber.e("SOEF T DISPLAY obstPoped "
          + obstaclePoped.getId()
          + " "
          + obstaclePoped.getX()
          + " "
          + obstaclePoped.getY());
      Rect dstObsc = new Rect(obstaclePoped.getX(), obstaclePoped.getY(),
          obstaclePoped.getX() + BirdRushObstacle.wiewWidth,
          Math.round(obstaclePoped.getY() + obstaclePoped.getViewHeight()));
      obstaclePopedList.put(obstaclePoped, dstObsc);
      canvas.drawBitmap(obstacleBtm, null, dstObsc, null);

      obstaclePoped = null;
    }
  }

  int index = 1;

  @SuppressLint("WrongConstant") private void displayBirds(Canvas canvas) {
    /**
     *  Move MY BIRD
     */

    for (Map.Entry<BirdRush, Rect> entry : birdList.entrySet()) {
      BirdRush birdRush = entry.getKey();
      Rect rect = entry.getValue();

      rect.set(birdRush.getX(), birdRush.getY(), birdRush.getX() + birdBtm.getWidth(),
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

      if (birdRush.isLost() || !birdRush.getGuestId().equals(currentUser.getId())) {
        myBirdPaint.setAlpha(255 / 2);
      } else {
        myBirdPaint.setAlpha(255);
      }

      if (birdRush.isLost()) {
        Timber.e("ROTATE BIRD ");
        int pivotX = birdRush.getX();
        int pivotY = birdRush.getY();
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(updateRotationBird(birdRush), pivotX, pivotY);
        canvas.drawBitmap(birdRush.getBitmap(), null, rect, myBirdPaint);
        canvas.restore();
        if (birdRush.getY() > screenHeight) {
          birdRush.setLost(false);
          killBirdTimer.unsubscribe();
          killBirdTimer = null;
        }
      } else {
        canvas.drawBitmap(birdRush.getBitmap(), null, rect, myBirdPaint);
        canvas.drawBitmap(birdRush.getBackgroundBitmap(), null, dstnameLabelBird, myBirdPaint);
        canvas.drawText(text, nameXPos, nameYPos + screenUtils.dpToPx(18), paint);
      }
    }
  }

  private void handleCollision(Canvas canvas) {
    BirdRush myBird = getMyBird();

    for (int i = 0; i < obstaclesList.size(); i++) {
      BirdRushObstacle o = obstaclesList.get(i);
      if (o != null && myBird != null && o.getX() > 0 && o.getX() > screenUtils.getWidthPx() / 2) {

        if (o.getX() - o.getViewWidth() < (screenUtils.getWidthPx() / 2)) {
          if (isBetween(o.getY(), o.getY() + o.getViewHeight(), myBird.getY())) {
            o.setHit(true);
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
    killBird();
  }

  private void moveBackBackground() {
    xScroll = xScroll - speedPx;
    if (xScroll < -screenWidth) xScroll = 0;
  }

  private void moveObstacleList() {
    for (Map.Entry<BirdRushObstacle, Rect> entry : obstaclePopedList.entrySet()) {
      BirdRushObstacle o = entry.getKey();
      o.setX(o.getX() - speedPx);
      o.setY(o.getY() + yTranslation(o));
    }
  }

  private void startBirds() {
    int v = 15;
    int i = 0;

    BirdRush myBird = getMyBird();
    myBird.setLost(false);

    Handler handler1 = new Handler();
    for (Map.Entry<BirdRush, Rect> entry : birdList.entrySet()) {
      int finalI1 = i;
      handler1.postDelayed(() -> {
        BirdRush b = entry.getKey();
        Rect rect = entry.getValue();

        if (b.getX() < xCenterBirdPos) {              // entrance of the bird
          b.setX(b.getX() + v);
        } else {
          b.setX(xCenterBirdPos);                     // middle of the screen
        }
        int coefIndex = (birdList.size() - finalI1) / birdList.size();
        int vitesse = (v * (yInitTranslation)) / (xCenterBirdPos) + coefIndex;

        if (b.getY() > yCenterBirdPos) {              // entrance of the bird
          b.setY(b.getY() - vitesse);
        } else {
          b.setY(yCenterBirdPos);
        }

        if (b.getX() >= xCenterBirdPos
            && b.getY() >= yCenterBirdPos
            && finalI1 == birdList.size() - 1) {

          subscriptions.add(Observable.timer((1000), TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aLong -> {
                entranceBirdFinish = true;
              }));
        }
      }, 300 * i);
      i++;
    }
  }

  private void killBird() {
    BirdRush b = getMyBird();
    b.setLost(true);
    if (killBirdTimer == null) {
      subscriptions.add(killBirdTimer = Observable.interval(0, 16, TimeUnit.MILLISECONDS)
          .onBackpressureDrop()
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            Timber.e("SOEF kill bird " + " " + aLong);
            b.setY(b.getY() + 20);
            b.setX(b.getX() - 2);
            invalidate();
          }));
    }
  }

  private void moveBirds() {
    for (Map.Entry<BirdRush, Rect> entry : birdList.entrySet()) {
      BirdRush bird = entry.getKey();
      bird.setX(xCenterBirdPos);

      if (bird.getSpeedY() < 0) {
        bird.setSpeedY(
            bird.getSpeedY() * 2 / 3 + getSpeedTimeDecrease() / 5);  // The character is moving up
      } else {
        bird.setSpeedY(bird.getSpeedY() + getSpeedTimeDecrease());  // the character is moving down
      }

      if (bird.getSpeedY() > getMaxSpeed()) {
        bird.setSpeedY(getMaxSpeed());  // speed limit
      }
      //  Timber.e("SOEF BIRD FALL " + Math.round(bird.getY() + bird.getSpeedY()));
      bird.setY(Math.round(bird.getY() + bird.getSpeedY()));
    }
  }

  public void jumpBird(String guestId, PlayerTap playerTap) {
    BirdRush b = getBird(guestId);
    b.setSpeedY(getTabSpeed());
    if (playerTap == null) {
      b.setY(b.getY() + getPosTabIncrease());
    } else {
      int y = (int) (GameBirdRushView.HEIGHT_IOS_SCREEN - playerTap.getY());
      b.setY(y + getPosTabIncrease());
    }
  }

  protected float getSpeedTimeDecrease() {
    float speed = screenUtils.getHeightPx() / 640;
    return speed;
  }

  protected float getMaxSpeed() {
    float speed = screenUtils.getHeightPx() / 81.2f;
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

    if (scrollTimer == null) {
      subscriptions.add(scrollTimer = Observable.interval(0, 16, TimeUnit.MILLISECONDS)
          .onBackpressureDrop()
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            startPlaying();
          }));
    }

    if (engineTimer == null) {
      subscriptions.add(engineTimer = Observable.interval(0, 100, TimeUnit.MILLISECONDS)
          .onBackpressureDrop()
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(this::popObstacles));
    }
  }

  private void startPlaying() {
    if (!pause) {
      if (!entranceBirdFinish) {
        startBirds();
      } else {
        moveBackBackground();
        moveObstacleList();
        moveBirds();
      }
    }
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
          Timber.e("SOEF T : "
              + i
              + " "
              + obstaclePoped.getId()
              + " "
              + obstaclePoped.getX()
              + " "
              + obstaclePoped.getY());
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
    for (Map.Entry<BirdRush, Rect> entry : birdList.entrySet()) {
      BirdRush birdRush = entry.getKey();
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
    for (Map.Entry<BirdRush, Rect> entry : birdList.entrySet()) {
      BirdRush b = entry.getKey();
      if (b.isMine()) return b;
    }
    return null;
  }

  public BirdRush getBird(String guestId) {
    for (Map.Entry<BirdRush, Rect> entry : birdList.entrySet()) {
      BirdRush b = entry.getKey();
      if (b.getGuestId().equals(guestId)) return b;
    }
    return null;
  }

  private void initBirdPosition(BirdRush birdRush) {
    int yPos =
        (screenHeight / 2) - (birdBtm.getHeight() / 2) - screenUtils.dpToPx(10) + yInitTranslation;
    int xPos = -2 * birdBtm.getWidth();
    birdRush.setX(xPos);
    birdRush.setY(yPos);
  }

  public void addBird(BirdRush birdRush, int index) {
    birdRush.setBitmap(getResources());
    initBirdPosition(birdRush);

    Rect dstBird = new Rect(birdRush.getX(), birdRush.getY(), birdRush.getX() + birdBtm.getWidth(),
        birdRush.getY() + birdBtm.getHeight());

    dstnameLabelBird = new Rect(birdRush.getX(), birdRush.getY() + birdBtm.getHeight(),
        birdRush.getX() + birdBtm.getWidth(), birdRush.getY() + birdBtm.getHeight());

    birdList.put(birdRush, dstBird);
  }

  public void start() {
    pause = false;
    resetParams(true);
    setTimer();
  }

  public void stop() {
    pause = true;
    Timber.e("SOEF BACKGROUND  stop ");
    //  clearObstacles();
    // invalidate();
  }

  public void dispose() {
    Timber.e("SOEF BACKGROUND  dispose ");
    resetParams(true);
    subscriptions.clear();
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

  public void resetParams(boolean ok) {
    Timber.e("SOEF resetParams");
    obstacleBtm = BitmapFactory.decodeResource(getResources(), R.drawable.game_birdrush_obstacle);
    entranceBirdFinish = false;
    displayFirstObstacle = false;
    engineTimer = null;
    scrollTimer = null;
    crossObstacle.clear();

    if (ok) {
      for (Map.Entry<BirdRush, Rect> entry : birdList.entrySet()) {
        BirdRush b = entry.getKey();
        initBirdPosition(b);
      }
      clearObstacles();
      subscriptions.clear();
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
