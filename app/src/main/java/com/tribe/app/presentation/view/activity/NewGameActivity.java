package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.tribelivesdk.game.Game;

public class NewGameActivity extends GameActivity {

  public static Intent getCallingIntent(Activity activity) {
    Intent intent = new Intent(activity, NewGameActivity.class);
    return intent;
  }

  // VARIABLES
  private UserComponent userComponent;

  // RESOURCES

  // OBSERVABLES

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    onGames.onNext(gameManager.getGames());
  }

  @Override protected void onGameSelected(Game game) {
    Intent intent = new Intent();
    if (game != null) intent.putExtra(GAME_ID, game.getId());
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override protected int getContentView() {
    return R.layout.activity_new_game;
  }

  @Override protected void initDependencyInjector() {
    this.userComponent = DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build();

    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  /**
   * ONCLICK
   */

  @OnClick(R.id.btnBack) void onClose() {
    finish();
  }

  /**
   * PUBLIC
   */
  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
  }

  /**
   * OBSERVABLES
   */
}
