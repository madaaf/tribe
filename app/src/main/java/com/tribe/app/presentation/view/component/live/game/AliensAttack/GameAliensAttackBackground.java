package com.tribe.app.presentation.view.component.live.game.AliensAttack;

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

  private static final int DURATION = 6000;

  @Inject ScreenUtils screenUtils;

  /**
   * VARIABLES
   */

  private Unbinder unbinder;
  private List<ImageView> viewsCar;

  /**
   * RESOURCES
   */

  private int marginBottomCar, marginLeftCar;

  /**
   * OBSERVABLES
   */

  private CompositeSubscription subscriptions = new CompositeSubscription();

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
    marginBottomCar = screenUtils.dpToPx(86);
    marginLeftCar = screenUtils.dpToPx(50);
  }

  private void initView() {
    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_game_aliens_attack_background, this, true);
    unbinder = ButterKnife.bind(this);

    setBackgroundResource(R.drawable.game_aliens_attack_bg);

    viewsCar = new ArrayList<>();
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

    initAnimations();
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

  private void initAnimations() {
    subscriptions.add(Observable.interval(0,
        DURATION * CARS_COUNT - ((int) (DURATION * 0.10f) * (CARS_COUNT - 1)),
        TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
      for (int i = 0; i < CARS_COUNT; i++) {
        ImageView car = viewsCar.get(i);
        car.clearAnimation();
        car.setTranslationX(0);
        car.animate()
            .translationX(screenUtils.getWidthPx() + marginLeftCar)
            .setDuration(DURATION)
            .setStartDelay(i * (int) (DURATION * 0.90f))
            .setInterpolator(new LinearInterpolator())
            .start();
      }
    }));
  }

  /**
   * PUBLIC
   */

  public void dispose() {
    for (int i = 0; i < viewsCar.size(); i++) {
      View view = viewsCar.get(i);
      view.clearAnimation();
    }

    subscriptions.clear();
  }

  /**
   * OBSERVABLES
   */

}
