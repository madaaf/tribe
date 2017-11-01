package com.tribe.app.presentation.view.component.live.game.AliensAttack;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.Random;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/01/2017.
 */

public class GameAliensAttackAlienView extends FrameLayout {

  @IntDef({ YELLOW, BLUE }) public @interface AlienType {
  }

  public static final int YELLOW = 0;
  public static final int BLUE = 1;

  @Inject ScreenUtils screenUtils;

  /**
   * VARIABLES
   */

  private ImageView imgAlien;
  private ImageView imgAlienBG;
  private ImageView imgAlienLost;
  private int alienType;

  /**
   * RESOURCES
   */

  /**
   * OBSERVABLES
   */

  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GameAliensAttackAlienView(@NonNull Context context, @AlienType int type) {
    super(context);
    this.alienType = type;
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    dispose();
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

  private void init() {
    initDependencyInjector();
    initResources();
    initView();
  }

  private void initResources() {

  }

  private void initView() {
    int drawableAlienId, drawableAlienBGId;

    if (alienType == BLUE) {
      drawableAlienId = R.drawable.game_aliens_attack_alien_1;
      drawableAlienBGId = R.drawable.game_aliens_attack_alien_gradient_blue;
    } else {
      int alienIndex = new Random().nextInt((3 - 2) + 2) + 2;
      drawableAlienId =
          getResources().getIdentifier("game_aliens_attack_alien_" + alienIndex, "drawable",
              getContext().getPackageName());
      drawableAlienBGId = R.drawable.game_aliens_attack_alien_gradient_yellow;
    }

    imgAlienBG = new ImageView(getContext());
    FrameLayout.LayoutParams paramsAlienBG =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
    paramsAlienBG.gravity = Gravity.CENTER;
    imgAlienBG.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    imgAlienBG.setImageResource(drawableAlienBGId);
    addView(imgAlienBG, paramsAlienBG);

    imgAlien = new ImageView(getContext());
    FrameLayout.LayoutParams paramsAlien =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
    paramsAlien.gravity = Gravity.BOTTOM;
    paramsAlien.topMargin = screenUtils.dpToPx(50);
    imgAlien.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    imgAlien.setImageResource(drawableAlienId);
    addView(imgAlien, paramsAlien);

    //imgAlienLost = new ImageView(getContext());
    //FrameLayout.LayoutParams paramsAlienLost =
    //    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
    //        FrameLayout.LayoutParams.WRAP_CONTENT);
    //paramsAlienLost.gravity = Gravity.BOTTOM;
    //imgAlienLost.setVisibility(View.GONE);
    //imgAlienLost.setImageResource(R.drawable.game_alien_bl);
  }

  /**
   * PUBLIC
   */

  public void dispose() {
    subscriptions.clear();
  }

  /**
   * OBSERVABLES
   */

}
