package com.tribe.app.presentation.view.component.games;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
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
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/07/2017.
 */

public class LeaderboardMainView extends FrameLayout {

  @Inject ScreenUtils screenUtils;

  @Inject LeaderboardUserAdapter adapter;

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

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
  private User user;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Game> onClick = PublishSubject.create();

  public LeaderboardMainView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    items = new ArrayList<>();
    gameManager = GameManager.getInstance(getContext());

    initDependencyInjector();
    initSubscriptions();
    initPresenter();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    gamePresenter.onViewDetached();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();
  }

  private void initPresenter() {
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
        return getContext();
      }
    };
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  /**
   * PUBLIC
   */

  public void setUser(User user, boolean collapsed) {
    this.user = user;

    adapter.setCanClick(!collapsed);

    txtEmojiGame.setEmojiList(user.getEmojiLeaderGameList());
    viewNewAvatar.load(user.getProfilePicture());
    txtName.setText(user.getDisplayName());
    txtUsername.setText(user.getUsernameDisplay());

    layoutManager = new LeaderboardUserLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new BaseListDividerDecoration(getContext(),
        ContextCompat.getColor(getContext(), R.color.grey_divider), screenUtils.dpToPx(0.5f)));

    subscriptions.add(adapter.onClick()
        .map(view -> adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view)))
        .subscribe(score -> onClick.onNext(score.getGame())));

    if (collapsed) {
      appBarLayout.setExpanded(false);
      UIUtils.changeHeightOfView(collapsingToolbar, 0);
    }

    gamePresenter.loadUserLeaderboard(user.getId());
  }

  /**
   * OBSERVABLES
   */

  public Observable<Game> onClick() {
    return onClick;
  }
}
