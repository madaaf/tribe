package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.adapter.ShortcutMVPViewAdapter;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.component.games.LeaderboardDetailsView;
import com.tribe.app.presentation.view.component.games.LeaderboardMainView;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.tribelivesdk.game.Game;
import javax.inject.Inject;

public class LeaderboardActivity extends ViewStackActivity {

  private static final String USER_ID = "USER_ID";
  private static final String DISPLAY_NAME = "DISPLAY_NAME";
  private static final String PROFILE_PICTURE = "PROFILE_PICTURE";

  public static Intent getCallingIntent(Activity activity) {
    Intent intent = new Intent(activity, LeaderboardActivity.class);
    return intent;
  }

  public static Intent getCallingIntent(Activity activity, String userId, String displayName,
      String profilePicture) {
    Intent intent = new Intent(activity, LeaderboardActivity.class);
    intent.putExtra(USER_ID, userId);
    intent.putExtra(DISPLAY_NAME, displayName);
    intent.putExtra(PROFILE_PICTURE, profilePicture);
    return intent;
  }

  @Inject ShortcutPresenter shortcutPresenter;

  // VIEWS
  private LeaderboardMainView viewLeaderboardMain;
  private LeaderboardDetailsView viewLeaderboardDetails;

  // VARIABLES
  private Game selectedGame;
  private String displayName, profilePicture, userId;
  private ShortcutMVPViewAdapter shortcutMVPViewAdapter;

  // OBSERVABLES

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    shortcutMVPViewAdapter = new ShortcutMVPViewAdapter() {
      @Override public void onShortcut(Shortcut shortcut) {
        setupMainView(shortcut.getSingleFriend(), true);
        newAvatarView.setType(
            shortcut.getSingleFriend().isOnline() ? NewAvatarView.ONLINE : NewAvatarView.NORMAL);
        shortcutPresenter.unsubscribeShortcut();
      }
    };
  }

  @Override protected void onStart() {
    super.onStart();
    shortcutPresenter.onViewAttached(shortcutMVPViewAdapter);
  }

  @Override protected void onStop() {
    super.onStop();
    shortcutPresenter.onViewDetached();
  }

  @Override protected void onDestroy() {
    if (viewLeaderboardDetails != null) viewLeaderboardDetails.onDestroy();
    if (viewLeaderboardMain != null) viewLeaderboardMain.onDestroy();
    super.onDestroy();
  }

  @Override protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  @Override protected void endInit(Bundle savedInstanceState) {
    if (getIntent().hasExtra(USER_ID)) {
      userId = getIntent().getStringExtra(USER_ID);
      displayName = getIntent().getStringExtra(DISPLAY_NAME);
      profilePicture = getIntent().getStringExtra(PROFILE_PICTURE);

      newAvatarView.setVisibility(View.VISIBLE);
      newAvatarView.load(profilePicture);
    }

    btnBack.setVisibility(View.VISIBLE);
    btnBack.setImageResource(R.drawable.picto_arrow_back);
    btnForward.setVisibility(View.GONE);

    if (StringUtils.isEmpty(userId)) {
      txtTitle.setText(R.string.leaderboards_you);
    } else {
      txtTitle.setText(displayName);
    }

    if (savedInstanceState == null && StringUtils.isEmpty(userId)) {
      setupMainView(getCurrentUser(), false);
    } else {
      shortcutPresenter.shortcutForUserIds(userId);
    }
  }

  @OnClick(R.id.btnBack) void clickBack() {
    onBackPressed();
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  private void setupMainView(User user, boolean collapsed) {
    viewLeaderboardMain = (LeaderboardMainView) viewStack.push(R.layout.view_leaderboard);
    viewLeaderboardMain.setUser(user, collapsed);

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
      setupTitle(getString(R.string.leaderboards_you), forward);
      btnForward.setVisibility(View.GONE);
      btnBack.setImageResource(R.drawable.picto_arrow_back);

      Glide.clear(btnBack);
    } else if (to instanceof LeaderboardDetailsView) {
      btnForward.setVisibility(View.GONE);
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
