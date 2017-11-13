package com.tribe.app.presentation.view.component.live.game.aliensattack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameAliensAttackBackground extends FrameLayout {

  private static final int CARS_COUNT = 2;
  private static final int CLOUDS_COUNT = 4;
  private static final int STARS_COUNT = 5;

  private static final int MARGIN_BOTTOM_CAR = 86;
  private static final int MARGIN_LEFT_CAR = 50;
  private static final int MARGIN_LEFT_CLOUD = 30;
  private static final int Y_LIMIT_SKY = 220;
  private static final int X_LIMIT_SKY = 0;

  private static final int DURATION_CARS = 10000;
  private static final int MIN_DURATION_CLOUDS = 35000;
  private static final int MAX_DURATION_CLOUDS = 60000;

  @Inject ScreenUtils screenUtils;

  /**
   * VARIABLES
   */

  private Unbinder unbinder;
  private List<ImageView> viewsCar;
  private List<ImageView> viewsCloud;
  private List<ImageView> viewsStar;

  /**
   * RESOURCES
   */

  private int marginBottomCar, marginLeftCar, marginLeftCloud, yLimitSky, xLimitSky;

  /**
   * OBSERVABLES
   */

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsAnimation = new CompositeSubscription();

  public GameAliensAttackBackground(@NonNull Context context) {
    super(context);
    init();
  }

  public GameAliensAttackBackground(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    initDependencyInjector();
    initResources();
    initView();
  }

  private void initResources() {
    marginBottomCar = screenUtils.dpToPx(MARGIN_BOTTOM_CAR);
    marginLeftCar = screenUtils.dpToPx(MARGIN_LEFT_CAR);
    marginLeftCloud = screenUtils.dpToPx(MARGIN_LEFT_CLOUD);
    yLimitSky = screenUtils.dpToPx(Y_LIMIT_SKY);
    xLimitSky = screenUtils.dpToPx(X_LIMIT_SKY);

    viewsCar = new ArrayList<>();
    viewsCloud = new ArrayList<>();
    viewsStar = new ArrayList<>();
  }

  private void initView() {
    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_game_aliens_attack_background, this, true);
    unbinder = ButterKnife.bind(this);

    setBackgroundResource(R.drawable.game_aliens_attack_bg);

    positionCars();
    positionSky();
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

  private void positionCars() {
    ImageView imageView;

    for (int i = 0; i < CARS_COUNT; i++) {
      imageView = new ImageView(getContext());
      FrameLayout.LayoutParams params =
          new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
              FrameLayout.LayoutParams.WRAP_CONTENT);
      params.gravity = Gravity.BOTTOM;
      params.bottomMargin = marginBottomCar;
      params.leftMargin = -marginLeftCar;
      imageView.setImageResource(R.drawable.game_aliens_attack_car);

      viewsCar.add(imageView);
      addView(imageView, params);
    }
  }

  private void positionSky() {
    clearSky();
    ImageView imageView;

    for (int i = 0; i < CLOUDS_COUNT; i++) {
      imageView = new ImageView(getContext());
      imageView.setId(View.generateViewId());
      FrameLayout.LayoutParams params =
          new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
              FrameLayout.LayoutParams.WRAP_CONTENT);
      params.gravity = Gravity.TOP;
      params.topMargin = randomYForCloud(i, CLOUDS_COUNT);
      params.leftMargin = -marginLeftCloud;
      imageView.setImageResource(
          getResources().getIdentifier("game_aliens_attack_cloud_" + randInt(1, 3), "drawable",
              getContext().getPackageName()));
      imageView.setTranslationX(randomXForSky(i, CLOUDS_COUNT));
      viewsCloud.add(imageView);
      addView(imageView, params);
    }

    for (int i = 0; i < STARS_COUNT; i++) {
      imageView = new ImageView(getContext());
      imageView.setId(View.generateViewId());
      FrameLayout.LayoutParams params =
          new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
              FrameLayout.LayoutParams.WRAP_CONTENT);
      params.gravity = Gravity.TOP;
      params.topMargin = randomYForStar();
      params.leftMargin = randomXForSky(i, STARS_COUNT);
      imageView.setImageResource(R.drawable.game_aliens_attack_star);

      viewsCloud.add(imageView);
      addView(imageView, params);
    }
  }

  private int randomXForSky(int i, int nb) {
    return randInt(xLimitSky, screenUtils.getWidthPx());
  }

  private int randomYForCloud(int i, int nb) {
    int portion = yLimitSky / nb;
    int rangeAvailable = portion * i;
    return randInt(rangeAvailable, rangeAvailable + portion);
  }

  private int randomYForStar() {
    return randInt(0, yLimitSky);
  }

  public static int randInt(int min, int max) {
    Random rand = new Random();
    int randomNum = rand.nextInt((max - min) + 1) + min;
    return randomNum;
  }

  private void initAnimations() {
    subscriptionsAnimation.add(Observable.interval(0,
        DURATION_CARS * CARS_COUNT - ((int) (DURATION_CARS * 0.10f) * (CARS_COUNT - 1)),
        TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
      for (int i = 0; i < CARS_COUNT; i++) {
        ImageView car = viewsCar.get(i);
        car.clearAnimation();
        car.setTranslationX(0);
        car.animate()
            .translationX(screenUtils.getWidthPx() + marginLeftCar)
            .setDuration(DURATION_CARS)
            .setStartDelay(i * (int) (DURATION_CARS * 0.90f))
            .setInterpolator(new LinearInterpolator())
            .start();
      }
    }));

    for (int i = 0; i < CLOUDS_COUNT; i++) {
      animateCloud(viewsCloud.get(i));
    }
  }

  private void animateCloud(ImageView cloud) {
    cloud.animate()
        .translationX(cloud.getTranslationX() +
            (screenUtils.getWidthPx() - cloud.getTranslationX()) +
            marginLeftCloud)
        .setDuration(randInt(MIN_DURATION_CLOUDS, MAX_DURATION_CLOUDS))
        .setInterpolator(new LinearInterpolator())
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            animation.removeAllListeners();
            cloud.animate().translationX(0).setDuration(0).setListener(null).start();
            animateCloud(cloud);
          }
        })
        .start();
  }

  private void clearCars() {
    for (int i = 0; i < viewsCar.size(); i++) {
      View view = viewsCar.get(i);
      view.clearAnimation();
    }
  }

  private void clearSky() {
    for (int i = 0; i < viewsStar.size(); i++) {
      View view = viewsStar.get(i);
      view.clearAnimation();
    }

    for (int i = 0; i < viewsCloud.size(); i++) {
      View view = viewsCloud.get(i);
      view.clearAnimation();
      view.animate().setListener(null).start();
    }
  }

  /**
   * PUBLIC
   */

  public void start() {
    initAnimations();
  }

  public void stop() {

  }

  public int getRoadBottomMargin() {
    return screenUtils.dpToPx(60);
  }

  public void dispose() {
    clearCars();
    clearSky();
    subscriptions.clear();
    subscriptionsAnimation.clear();
  }

  /**
   * OBSERVABLES
   */

}
