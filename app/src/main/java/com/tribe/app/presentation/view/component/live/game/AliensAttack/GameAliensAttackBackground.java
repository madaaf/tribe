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

public class GameAliensAttackBackground extends GameView {

  public GameAliensAttackBackground(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public GameAliensAttackBackground(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    inflater.inflate(R.layout.view_game_aliens_attack_background, this, true);
    unbinder = ButterKnife.bind(this);

    setBackgroundResource(R.drawable.game_aliens_attack_bg);
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
