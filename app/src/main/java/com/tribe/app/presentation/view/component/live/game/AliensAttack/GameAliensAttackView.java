package com.tribe.app.presentation.view.component.live.game.AliensAttack;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.component.live.game.GameView;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameAliensAttackView extends GameView {

  // VARIABLES

  private GameAliensAttackEngine engine;

  public GameAliensAttackView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public GameAliensAttackView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    engine.stop();
  }

  private void initView(Context context) {
    inflater.inflate(R.layout.view_game_aliens_attack, this, true);
    unbinder = ButterKnife.bind(this);

    engine = new GameAliensAttackEngine(context);
    engine.start();
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

  @Override public void dispose() {
    super.dispose();
  }

  @Override public void setNextGame() {

  }

  @Override protected void initWebRTCRoomSubscriptions() {

  }

  /**
   * PUBLIC
   */

  /**
   * OBSERVABLES
   */

}
