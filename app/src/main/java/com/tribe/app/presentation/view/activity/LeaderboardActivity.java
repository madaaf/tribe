package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.mvp.view.adapter.UserMVPViewAdapter;
import com.tribe.app.presentation.view.adapter.LeaderboardUserAdapter;
import com.tribe.app.presentation.view.adapter.decorator.BaseListDividerDecoration;
import com.tribe.app.presentation.view.adapter.manager.LeaderboardUserLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.EmojiGameView;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

public class LeaderboardActivity extends BaseActivity {

  public static final String USER = "USER";

  public static Intent getCallingIntent(Activity activity, User user) {
    Intent intent = new Intent(activity, LeaderboardActivity.class);
    intent.putExtra(USER, user);
    return intent;
  }

  public static final String GAME_ID = "game_id";

  @Inject User currentUser;

  @Inject ScreenUtils screenUtils;

  @Inject LeaderboardUserAdapter adapter;

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.recyclerViewLeaderboard) RecyclerView recyclerView;

  @BindView(R.id.viewNewAvatar) NewAvatarView viewNewAvatar;

  @BindView(R.id.txtEmojiGame) EmojiGameView txtEmojiGame;

  @BindView(R.id.txtName) TextViewFont txtName;

  @BindView(R.id.txtUsername) TextViewFont txtUsername;

  @BindView(R.id.appBar) AppBarLayout appBarLayout;

  @BindView(R.id.collapsingToolbar) CollapsingToolbarLayout collapsingToolbar;

  // VARIABLES
  private LeaderboardUserLayoutManager layoutManager;
  private List<Score> items;
  private GameManager gameManager;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private UserMVPViewAdapter userMVPViewAdapter;
  private User user;

  // RESOURCES

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    getWindow().getDecorView().setBackgroundColor(Color.WHITE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_leaderboard);

    ButterKnife.bind(this);

    gameManager = GameManager.getInstance(this);
    items = new ArrayList<>();

    initDependencyInjector();
    initPresenter();
    initSubscriptions();
    initUI();
    setUser(user, user.equals(currentUser));
  }

  @Override protected void onStart() {
    super.onStart();
  }

  @Override protected void onStop() {
    super.onStop();
    gamePresenter.onViewDetached();
  }

  @Override protected void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  protected void initPresenter() {
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public void onUserLeaderboard(List<Score> newScoreList, boolean cloud) {
        Set<String> gameIdPresent = new HashSet<>();
        List<Score> endScoreList = new ArrayList<>();

        for (Score score : newScoreList) {
          endScoreList.add(score);
          gameIdPresent.add(score.getGame().getId());
        }

        for (Game game : gameManager.getGames()) {
          if (!gameIdPresent.contains(game.getId()) && game.hasScores()) {
            Score score = new Score();
            score.setGame(game);
            score.setUser(user);
            endScoreList.add(score);
          }
        }

        items.clear();
        items.addAll(endScoreList);
        adapter.setItems(items);
      }

      @Override public Context context() {
        return LeaderboardActivity.this;
      }
    };
  }

  protected void initSubscriptions() {
    subscriptions = new CompositeSubscription();
  }

  private void initUI() {

  }

  private void initDependencyInjector() {
    this.getApplicationComponent().inject(this);
  }

  private void setUser(User user, boolean collapsed) {
    this.user = user;

    adapter.setCanClick(!collapsed);
    adapter.setUser(user);

    txtEmojiGame.setEmojiList(user.getEmojiLeaderGameList());
    viewNewAvatar.load(user.getProfilePicture());
    txtName.setText(user.getDisplayName());
    txtUsername.setText(user.getUsernameDisplay());

    layoutManager = new LeaderboardUserLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(
        new BaseListDividerDecoration(this, ContextCompat.getColor(this, R.color.grey_divider),
            screenUtils.dpToPx(0.5f)));

    subscriptions.add(adapter.onClick()
        .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)))
        .subscribe(score -> {
          // TODO
        }));

    if (collapsed) {
      appBarLayout.setExpanded(false);
      UIUtils.changeHeightOfView(collapsingToolbar, 0);
    }

    gamePresenter.loadUserLeaderboard(user.getId());
  }

  /**
   * ONCLICK
   */

  /**
   * PUBLIC
   */

  /**
   * OBSERVABLES
   */
}
