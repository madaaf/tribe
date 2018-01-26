package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.presenter.UserPresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.mvp.view.adapter.UserMVPViewAdapter;
import com.tribe.app.presentation.view.adapter.GameAdapter;
import com.tribe.app.presentation.view.adapter.decorator.BaseListDividerDecoration;
import com.tribe.app.presentation.view.adapter.manager.GamesLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameFooter;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class GameStoreActivity extends BaseActivity {

  public static final String SHORTCUT = "shortcut";
  public static final String CALL_ROULETTE = "call_roulette";
  public static final String GAME_ID = "game_id";

  public static Intent getCallingIntent(Activity activity) {
    Intent intent = new Intent(activity, GameStoreActivity.class);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @Inject GameAdapter gameAdapter;

  @Inject GamePresenter gamePresenter;

  @Inject UserPresenter userPresenter;

  @BindView(R.id.recyclerViewGames) RecyclerView recyclerViewGames;

  // VARIABLES
  private UserComponent userComponent;
  private GamesLayoutManager layoutManager;
  private List<Game> items;
  private GameManager gameManager;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private UserMVPViewAdapter userMVPViewAdapter;

  // RESOURCES

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<User> onUser = PublishSubject.create();
  private PublishSubject<List<Game>> onGames = PublishSubject.create();

  @Override protected void onCreate(Bundle savedInstanceState) {
    getWindow().getDecorView().setBackgroundColor(Color.WHITE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game_store);

    ButterKnife.bind(this);

    gameManager = GameManager.getInstance(this);
    items = new ArrayList<>();

    initDependencyInjector();
    initPresenter();
    initSubscriptions();
    initUI();
  }

  @Override protected void onStart() {
    super.onStart();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
    userPresenter.onViewAttached(userMVPViewAdapter);
  }

  @Override protected void onStop() {
    super.onStop();
    gamePresenter.onViewDetached();
    userPresenter.onViewDetached();
  }

  @Override protected void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  private void initPresenter() {
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public Context context() {
        return GameStoreActivity.this;
      }

      @Override public void onGameList(List<Game> gameList) {
        gameManager.addGames(gameList);
        onGames.onNext(gameList);
      }
    };

    userMVPViewAdapter = new UserMVPViewAdapter() {
      @Override public void onUserInfos(User user) {
        onUser.onNext(user);
      }
    };

    userPresenter.getUserInfos();
    gamePresenter.getGames();
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();

    subscriptions.add(Observable.combineLatest(onUser, onGames, (t1, t2) -> {
      items.clear();
      items.addAll(gameManager.getGames());
      addFooterItem();

      gameAdapter.setItems(items);
      return null;
    }).subscribe());
  }

  private void initUI() {
    layoutManager = new GamesLayoutManager(this);
    recyclerViewGames.setLayoutManager(layoutManager);
    recyclerViewGames.setItemAnimator(null);
    recyclerViewGames.setAdapter(gameAdapter);
    recyclerViewGames.addItemDecoration(
        new BaseListDividerDecoration(this, ContextCompat.getColor(this, R.color.grey_divider),
            screenUtils.dpToPx(0.5f)));

    subscriptions.add(gameAdapter.onClick()
        .map(view -> gameAdapter.getItemAtPosition(recyclerViewGames.getChildLayoutPosition(view)))
        .subscribe(game -> navigator.navigateToGameDetails(this, game.getId())));
  }

  private void addFooterItem() {
    GameFooter gameSupport = new GameFooter(this, Game.GAME_SUPPORT);
    items.add(gameSupport);
  }

  private void initDependencyInjector() {
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

  @OnClick(R.id.btnFriends) void onClickFriends() {
    navigator.navigateToHome(this);
  }

  @OnClick(R.id.btnLeaderboards) void onClickLeaderboards() {
    navigator.navigateToLeaderboards(this);
  }

  /**
   * PUBLIC
   */

  /**
   * OBSERVABLES
   */
}
