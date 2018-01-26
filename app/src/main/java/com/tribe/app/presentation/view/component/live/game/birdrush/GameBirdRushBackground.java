package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.animation.ValueAnimator;
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
import butterknife.Unbinder;
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

  // @BindView(R.id.background_one) ImageView backgroundOne;
  // @BindView(R.id.background_two) ImageView backgroundTwo;

  private static Bitmap splash = null;
  private Rect dstSplash;
  private Rect dstSplash2;

  /**
   * VARIABLES
   */

  private Unbinder unbinder;
  private ValueAnimator animator;

  int x = 0;
  int y = 0;
  /**
   * OBSERVABLES
   */

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsAnimation = new CompositeSubscription();

  public GameBirdRushBackground(@NonNull Context context) {
    super(context);
    init();
  }

  public GameBirdRushBackground(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    initDependencyInjector();
    initResource();
    initView();

    dstSplash = new Rect(x, y, x + screenUtils.getWidthPx(), y + screenUtils.getHeightPx());
    dstSplash2 = new Rect(x - screenUtils.getWidthPx(), y, x, y + screenUtils.getHeightPx());
  }

  @Override protected void onDraw(Canvas canvas) {
    Timber.e("ON DRAW " + x);

    dstSplash.set(x, y, x + screenUtils.getWidthPx(), y + screenUtils.getHeightPx());
    dstSplash2.set(x - screenUtils.getWidthPx(), y, x, y + screenUtils.getHeightPx());

    canvas.drawBitmap(splash, null, dstSplash, null);
    canvas.drawBitmap(splash, null, dstSplash2, null);
  }

  private void initResource() {
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        SPEED_BACK_SCROLL = (long) (screenUtils.getWidthPx() * 20);
      }
    });
  }

  private void initView() {
   /* LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_game_bird_rush_background, this, true);*/
    //    unbinder = ButterKnife.bind(this);

    positionBird();
    if (splash == null) {
      //splash = getBitmapAlpha8(getContext(), R.drawable.game_birdsrush_sky);
      splash = BitmapFactory.decodeResource(getResources(), R.drawable.game_birdsrush_sky);
    }
  }

  public static Bitmap getBitmapAlpha8(Context context, int id) {
    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
    bitmapOptions.inPreferredConfig = Bitmap.Config.ALPHA_8;
    return BitmapFactory.decodeResource(context.getResources(), id, bitmapOptions);
  }

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

  private void initAnimations() {
    // setBackScrolling();
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
  }

  private void setBackScrolling() {
    /*
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        Timber.e("SOEF SET BACK SCROLLING ");
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
    */
  }

  /**
   * PUBLIC
   */

  public void start() {
    initAnimations();
  }

  public void stop(List<BirdRushObstacle> obstaclesList) {
    Timber.e("SOEF BACKGRUND  stop ");
    if (animator != null) animator.cancel();
    for (int i = 0; i < obstaclesList.size(); i++) {
      View v = obstaclesList.get(i).getView();
      //removeView(v);
    }
    // backgroundOne.clearAnimation();
    // backgroundTwo.clearAnimation();
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

  public void draw() {
    x = x + 1;
    //if (x > screenUtils.getWidthPx()) x = 0;

    invalidate();
  }

  /**
   * OBSERVABLES
   */

}
