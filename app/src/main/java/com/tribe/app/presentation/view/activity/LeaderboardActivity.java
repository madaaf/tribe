package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.component.games.LeaderboardMainView;

public class LeaderboardActivity extends ViewStackActivity {

  public static Intent getCallingIntent(Activity activity) {
    Intent intent = new Intent(activity, LeaderboardActivity.class);
    return intent;
  }

  // VIEWS
  private LeaderboardMainView viewLeaderboardMain;

  // VARIABLES

  // OBSERVABLES

  @Override protected void endInit(Bundle savedInstanceState) {
    txtTitle.setText(R.string.new_game_title);

    if (savedInstanceState == null) {
      setupMainView();
    }
  }

  @OnClick(R.id.btnBack) void clickBack() {
    onBackPressed();
  }

  private void setupMainView() {
    viewLeaderboardMain = (LeaderboardMainView) viewStack.push(R.layout.view_game_store);
  }

  protected void computeTitle(boolean forward, View to) {
    if (to instanceof LeaderboardMainView) {
      ViewCompat.setElevation(btnBack, 0);
      btnBack.setImageResource(R.drawable.picto_btn_close);
      setupTitle(getString(R.string.leaderboards_you), forward);
      btnForward.setVisibility(View.VISIBLE);
    }
  }
}
