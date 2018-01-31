package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.UserPresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.mvp.view.adapter.UserMVPViewAdapter;
import com.tribe.tribelivesdk.game.Game;
import java.util.List;
import javax.inject.Inject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class GameStoreActivity extends GameActivity {

  public static Intent getCallingIntent(Activity activity) {
    Intent intent = new Intent(activity, GameStoreActivity.class);
    return intent;
  }

  @Inject UserPresenter userPresenter;

  // VARIABLES
  private UserComponent userComponent;
  private UserMVPViewAdapter userMVPViewAdapter;

  // RESOURCES

  // OBSERVABLES
  private PublishSubject<User> onUser = PublishSubject.create();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initPresenter();
  }

  @Override protected void onStart() {
    super.onStart();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
    userPresenter.onViewAttached(userMVPViewAdapter);
  }

  @Override protected void onStop() {
    super.onStop();
    userPresenter.onViewDetached();
    gamePresenter.onViewDetached();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }

  @Override protected void initPresenter() {
    super.initPresenter();
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public Context context() {
        return GameStoreActivity.this;
      }

      @Override public void onGameList(List<Game> gameList) {
        gameManager.addGames(gameList);
        onGames.onNext(gameList);
      }
    };

    gamePresenter.getGames();
    userMVPViewAdapter = new UserMVPViewAdapter() {
      @Override public void onUserInfos(User user) {
        onUser.onNext(user);
      }
    };

    userPresenter.getUserInfos();
  }

  @Override protected void initSubscriptions() {
    super.initSubscriptions();
  }

  @Override protected void onGameSelected(Game game) {
    navigator.navigateToGameDetails(this, game.getId());
  }

  @Override protected int getContentView() {
    return R.layout.activity_game_store;
  }

  protected void initDependencyInjector() {
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
