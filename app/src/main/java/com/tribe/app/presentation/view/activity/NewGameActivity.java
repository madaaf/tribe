package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.tribelivesdk.game.Game;

public class NewGameActivity extends GameActivity {

  public static final String EXTRA_SHORTCUT = "EXTRA_SHORTCUT";

  public static Intent getCallingIntent(Activity activity) {
    Intent intent = new Intent(activity, NewGameActivity.class);
    return intent;
  }

  public static Intent getCallingIntent(Activity activity, Shortcut shortcut) {
    Intent intent = new Intent(activity, NewGameActivity.class);
    intent.putExtra(EXTRA_SHORTCUT, shortcut);
    return intent;
  }

  // VARIABLES
  private UserComponent userComponent;
  private Shortcut shortcut;

  // RESOURCES

  // OBSERVABLES

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getIntent().hasExtra(EXTRA_SHORTCUT)) {
      shortcut = (Shortcut) getIntent().getSerializableExtra(EXTRA_SHORTCUT);
    }

    onGames.onNext(gameManager.getGames());
  }

  @Override protected void onGameSelected(Game game) {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.LAUNCHED);
    bundle.putString(TagManagerUtils.NAME, game.getTitle());

    if (shortcut == null) {
      Intent intent = new Intent();
      if (game != null) intent.putExtra(GAME_ID, game.getId());
      setResult(RESULT_OK, intent);
      bundle.putString(TagManagerUtils.SOURCE, TagManagerUtils.LIVE);
    } else {
      navigator.navigateToLive(this, shortcut, LiveActivity.SOURCE_SHORTCUT_ITEM,
          TagManagerUtils.SECTION_SHORTCUT, game.getId());
      bundle.putString(TagManagerUtils.SOURCE, TagManagerUtils.CHAT);
    }

    tagManager.trackEvent(TagManagerUtils.NewGame, bundle);

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
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.CANCELLED);
    bundle.putString(TagManagerUtils.SOURCE,
        shortcut == null ? TagManagerUtils.LIVE : TagManagerUtils.CHAT);
    tagManager.trackEvent(TagManagerUtils.NewGame, bundle);
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
