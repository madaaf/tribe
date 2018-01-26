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
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.List;
import javax.inject.Inject;
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
  private boolean pause = false;

  /**
   * OBSERVABLES
   */
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsAnimation = new CompositeSubscription();

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
    Timber.e("ON DRAW " + x);

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

  private void initAnimations() {
    // setBackScrolling();
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
  }

  /**
   * PUBLIC
   */

  public void start() {
    initAnimations();
    pause = false;
  }

  public void stop(List<BirdRushObstacle> obstaclesList) {
    pause = true;
    Timber.e("SOEF BACKGRUND  stop ");
    // if (animator != null) animator.cancel();
    for (int i = 0; i < obstaclesList.size(); i++) {
      View v = obstaclesList.get(i).getView();
      //removeView(v);
    }
  }

  public void dispose() {
    subscriptions.unsubscribe();
    subscriptionsAnimation.unsubscribe();
    Timber.e("SOEF BACKGRUND  dispose ");
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
