package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.component.games.GamesMembersView;
import com.tribe.app.presentation.view.component.games.GamesStoreView;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.tribelivesdk.game.Game;

import static android.view.View.GONE;

public class NewGameActivity extends ViewStackActivity {

  public static final String SHORTCUT = "shortcut";
  public static final String CALL_ROULETTE = "call_roulette";
  public static final String GAME_ID = "game_id";
  public static final String FROM_HOME = "from_home";
  public static final String SOURCE = "source";

  public static Intent getCallingIntent(Activity activity, String source) {
    Intent intent = new Intent(activity, NewGameActivity.class);
    intent.putExtra(FROM_HOME, activity instanceof HomeActivity);
    intent.putExtra(SOURCE, source);
    return intent;
  }

  // VIEWS
  private GamesStoreView viewGamesStore;
  private GamesMembersView viewGamesMembers;

  // VARIABLES
  private boolean isFromHome = false;
  private Game selectedGame = null;
  private String source;

  // OBSERVABLES

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getIntent() != null && getIntent().getExtras() != null) {
      isFromHome = getIntent().getBooleanExtra(FROM_HOME, false);
      source = getIntent().getStringExtra(SOURCE);
    }
  }

  @Override protected void endInit(Bundle savedInstanceState) {
    txtTitle.setText(R.string.new_game_title);

    if (savedInstanceState == null) {
      setupMainView();
    }

    txtAction.setOnClickListener(v -> {
      if (viewStack.getTopView() instanceof GamesMembersView) {
        progressView.setVisibility(View.VISIBLE);
        txtAction.setVisibility(View.GONE);
        viewGamesMembers.create();

        if (selectedGame != null) {
          Bundle bundle = new Bundle();
          bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.GAME);
          bundle.putString(TagManagerUtils.SOURCE, source);
          bundle.putString(TagManagerUtils.GAME, selectedGame.getId());
          tagManager.trackEvent(TagManagerUtils.Game, bundle);
        }
      }
    });

    btnForward.setOnClickListener(v -> {
      if (viewStack.getTopView() instanceof GamesStoreView) {
        setupMembersView(null);
      }
    });

    if (!isFromHome) {
      txtAction.setVisibility(View.GONE);
      btnForward.setVisibility(View.GONE);
    }
  }

  @Override public void onBackPressed() {
    if (viewStack.getTopView() instanceof GamesStoreView) {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.CANCEL);
      bundle.putString(TagManagerUtils.SOURCE, source);
      tagManager.trackEvent(TagManagerUtils.Game, bundle);
    }

    super.onBackPressed();
  }

  private void setupMainView() {
    viewGamesStore = (GamesStoreView) viewStack.push(R.layout.view_game_store);

    subscriptions.add(viewGamesStore.onGameClick().subscribe(game -> {
      if (isFromHome) {
        setupMembersView(game);
      } else {
        Intent intent = new Intent();
        if (game != null) intent.putExtra(GAME_ID, game.getId());
        setResult(RESULT_OK, intent);
        finish();
      }
    }));
  }

  private void setupMembersView(Game game) {
    selectedGame = game;

    viewGamesMembers = (GamesMembersView) viewStack.push(R.layout.view_game_members);
    viewGamesMembers.setGame(selectedGame);

    subscriptions.add(viewGamesMembers.onFinish().subscribe(shortcut -> {
      if (shortcut == null) {
        finish();
      } else {
        Intent intent = new Intent();
        intent.putExtra(SHORTCUT, shortcut);
        if (game != null) intent.putExtra(GAME_ID, selectedGame.getId());
        setResult(RESULT_OK, intent);
        finish();
      }
    }));

    subscriptions.add(
        viewGamesMembers.onHasMembers().subscribe(aBoolean -> txtAction.setEnabled(aBoolean)));

    subscriptions.add(viewGamesMembers.onCallRouletteSelected().subscribe(aVoid -> {
      Intent intent = new Intent();
      intent.putExtra(CALL_ROULETTE, true);
      intent.putExtra(GAME_ID, selectedGame.getId());
      setResult(RESULT_OK, intent);
      finish();
    }));
  }

  protected void computeTitle(boolean forward, View to) {
    if (to instanceof GamesStoreView) {
      ViewCompat.setElevation(btnBack, 0);
      btnBack.setImageResource(R.drawable.picto_btn_close);
    } else if (to instanceof GamesMembersView) {
      if (selectedGame == null) {
        ViewCompat.setElevation(btnBack, 0);
        btnBack.setImageResource(R.drawable.picto_btn_back);
      } else {
        new GlideUtils.GameImageBuilder(this, screenUtils).url(selectedGame.getIcon())
            .hasBorder(false)
            .hasPlaceholder(true)
            .rounded(true)
            .target(btnBack)
            .load();

        ViewCompat.setElevation(btnBack, screenUtils.dpToPx(5));
      }
    }

    if (to instanceof GamesStoreView) {
      setupTitle(getString(R.string.new_game_title), forward);
      txtAction.setVisibility(GONE);
      btnForward.setVisibility(View.VISIBLE);
    } else if (to instanceof GamesMembersView) {
      if (selectedGame == null) {
        setupTitle(getString(R.string.home_action_new_chat), forward);
      } else {
        setupTitle(selectedGame.getTitle(), forward);
      }

      btnForward.setVisibility(View.GONE);
      txtAction.setVisibility(View.VISIBLE);
      txtAction.setText(getString(R.string.action_create));
    }
  }
}
