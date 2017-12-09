package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.games.LeaderboardDetailsView;
import com.tribe.app.presentation.view.component.games.LeaderboardMainView;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.tribelivesdk.game.Game;

public class LeaderboardActivity extends ViewStackActivity {

  public static Intent getCallingIntent(Activity activity) {
    Intent intent = new Intent(activity, LeaderboardActivity.class);
    return intent;
  }

  // VIEWS
  private LeaderboardMainView viewLeaderboardMain;
  private LeaderboardDetailsView viewLeaderboardDetails;

  // VARIABLES
  private Game selectedGame;

  // OBSERVABLES

  @Override protected void endInit(Bundle savedInstanceState) {
    btnBack.setVisibility(View.GONE);
    txtTitle.setText(R.string.leaderboards_you);

    btnForward.setOnClickListener(v -> finish());

    if (savedInstanceState == null) {
      setupMainView();
    }
  }

  @OnClick(R.id.btnBack) void clickBack() {
    onBackPressed();
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_left);
  }

  private void setupMainView() {
    viewLeaderboardMain = (LeaderboardMainView) viewStack.push(R.layout.view_leaderboard);

    subscriptions.add(
        viewLeaderboardMain.onSettings().subscribe(aVoid -> navigator.navigateToProfile(this)));

    subscriptions.add(viewLeaderboardMain.onClick().subscribe(game -> {
      selectedGame = game;
      setupLeaderboardDetails();
    }));
  }

  private void setupLeaderboardDetails() {
    viewLeaderboardDetails =
        (LeaderboardDetailsView) viewStack.push(R.layout.view_leaderboard_details);
    viewLeaderboardDetails.setGame(selectedGame);
  }

  protected void computeTitle(boolean forward, View to) {
    if (to instanceof LeaderboardMainView) {
      btnBack.setVisibility(View.GONE);
      setupTitle(getString(R.string.leaderboards_you), forward);
      btnForward.setVisibility(View.VISIBLE);
    } else if (to instanceof LeaderboardDetailsView) {
      btnForward.setVisibility(View.GONE);
      btnBack.setVisibility(View.VISIBLE);
      setupTitle(selectedGame.getTitle(), forward);

      new GlideUtils.GameImageBuilder(this, screenUtils).url(selectedGame.getIcon())
          .hasBorder(false)
          .hasPlaceholder(true)
          .rounded(true)
          .target(btnBack)
          .load();

      ViewCompat.setElevation(btnBack, screenUtils.dpToPx(5));
    }
  }
}
