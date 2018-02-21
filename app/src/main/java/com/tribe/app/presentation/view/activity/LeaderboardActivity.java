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
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.view.adapter.LeaderboardUserAdapter;
import com.tribe.app.presentation.view.adapter.decorator.BaseListDividerDecoration;
import com.tribe.app.presentation.view.adapter.manager.LeaderboardUserLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.avatar.EmojiGameView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class LeaderboardActivity extends BaseBroadcastReceiverActivity {

  public static final String USER = "USER";

  public static Intent getCallingIntent(Activity activity, User user) {
    Intent intent = new Intent(activity, LeaderboardActivity.class);
    intent.putExtra(USER, user);
    return intent;
  }

  public static final String GAME_ID = "game_id";
  private static final int DURATION = 500;
  private static final float OVERSHOOT = 1.25f;

  @Inject User currentUser;

  @Inject ScreenUtils screenUtils;

  @Inject LeaderboardUserAdapter adapter;

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.recyclerViewLeaderboard) RecyclerView recyclerView;

  @BindView(R.id.viewAvatar) AvatarView viewAvatar;

  @BindView(R.id.txtEmojiGame) EmojiGameView txtEmojiGame;

  @BindView(R.id.txtName) TextViewFont txtName;

  @BindView(R.id.txtUsername) TextViewFont txtUsername;

  @BindView(R.id.appBar) AppBarLayout appBarLayout;

  @BindView(R.id.collapsingToolbar) CollapsingToolbarLayout collapsingToolbar;

  @BindView(R.id.layoutUser) RelativeLayout layoutUser;

  // VARIABLES
  private LeaderboardUserLayoutManager layoutManager;
  private List<Score> items;
  private GameManager gameManager;
  private GameMVPViewAdapter gameMVPViewAdapter;
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

    user = (User) getIntent().getSerializableExtra(USER);

    initDependencyInjector();
    initPresenter();
    initSubscriptions();
    initUI();
    setUser(user, !user.equals(currentUser));
  }

  @Override protected void onStart() {
    super.onStart();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
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
    appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
      float range = (float) -appBarLayout.getTotalScrollRange();
      layoutUser.setAlpha(1.0f - (float) verticalOffset / range);
    });

    recyclerView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            recyclerView.setTranslationY(recyclerView.getMeasuredHeight());
          }
        });
  }

  private void initDependencyInjector() {
    this.getApplicationComponent().inject(this);
  }

  private void setUser(User user, boolean collapsed) {
    this.user = user;

    adapter.setCanClick(!collapsed);
    adapter.setUser(user);

    txtEmojiGame.setEmojiList(user.getEmojiLeaderGameList());
    viewAvatar.load(user.getProfilePicture());
    txtName.setText(user.getDisplayName());
    txtUsername.setText(user.getUsernameDisplay());

    layoutManager = new LeaderboardUserLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(
        new BaseListDividerDecoration(this, ContextCompat.getColor(this, R.color.black_opacity_10),
            screenUtils.dpToPx(0.5f)));

    subscriptions.add(adapter.onClick()
        .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)))
        .subscribe(score -> navigator.navigateToGameLeaderboard(this, score.getGame().getId())));

    if (collapsed) {
      appBarLayout.setExpanded(false);
      UIUtils.changeHeightOfView(collapsingToolbar, 0);
    }

    gamePresenter.loadUserLeaderboard(user.getId());

    subscriptions.add(Observable.timer(DURATION, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          showAvatar();
          showRecyclerView();

          subscriptions.add(Observable.timer((int) (DURATION * 0.3f), TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aLong2 -> showDisplayName()));

          subscriptions.add(Observable.timer((int) (DURATION * 0.6f), TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aLong2 -> showUsername()));
        }));
  }

  private void showAvatar() {
    scaleUp(viewAvatar);
    scaleUp(txtEmojiGame);
  }

  private void showUsername() {
    scaleUp(txtUsername);
  }

  private void showDisplayName() {
    scaleUp(txtName);
  }

  private void showRecyclerView() {
    recyclerView.animate()
        .translationY(0)
        .setDuration(DURATION)
        .setInterpolator(new OvershootInterpolator(0.5f))
        .start();
  }

  private void scaleUp(View view) {
    if (view.getVisibility() != View.VISIBLE) return;

    view.setPivotX(view.getMeasuredWidth() >> 1);
    view.setPivotY(view.getMeasuredHeight() >> 1);

    view.animate()
        .scaleY(1)
        .scaleX(1)
        .setDuration(DURATION)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .start();
  }

  /**
   * ONCLICK
   */

  @OnClick(R.id.btnBack) void back() {
    onBackPressed();
  }

  /**
   * PUBLIC
   */

  /**
   * OBSERVABLES
   */
}
