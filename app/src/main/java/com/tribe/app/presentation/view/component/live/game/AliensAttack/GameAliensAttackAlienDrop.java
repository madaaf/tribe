package com.tribe.app.presentation.view.component.live.game.AliensAttack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameAliensAttackAlienDrop extends FrameLayout {

  @Inject ScreenUtils screenUtils;

  /**
   * VARIABLES
   */

  /**
   * RESOURCES
   */

  /**
   * OBSERVABLES
   */

  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GameAliensAttackAlienDrop(@NonNull Context context) {
    super(context);
    init();
  }

  public GameAliensAttackAlienDrop(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    dispose();
  }

  private void init() {
    initResources();
    initView();
  }

  private void initResources() {

  }

  private void initView() {

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
