package com.tribe.app.presentation.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
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
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public abstract class GameActivity extends BaseBroadcastReceiverActivity {

  public static final String GAME_ID = "game_id";

  @Inject ScreenUtils screenUtils;

  @Inject GameAdapter gameAdapter;

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.recyclerViewGames) RecyclerView recyclerViewGames;

  // VARIABLES
  protected GamesLayoutManager layoutManager;
  protected List<Game> items;
  protected GameManager gameManager;
  protected GameMVPViewAdapter gameMVPViewAdapter;

  // RESOURCES

  // OBSERVABLES
  protected CompositeSubscription subscriptions;
  protected PublishSubject<List<Game>> onGames = PublishSubject.create();

  @Override protected void onCreate(Bundle savedInstanceState) {
    getWindow().getDecorView().setBackgroundColor(Color.WHITE);
    super.onCreate(savedInstanceState);
    setContentView(getContentView());

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
  }

  protected void initSubscriptions() {
    subscriptions = new CompositeSubscription();

    subscriptions.add(onGames.subscribe(games -> {
      items.clear();
      items.addAll(gameManager.getGames());
      addFooterItem();

      gameAdapter.setItems(items);
    }));
  }

  private void initUI() {
    layoutManager = new GamesLayoutManager(this);
    recyclerViewGames.setLayoutManager(layoutManager);
    recyclerViewGames.setItemAnimator(null);
    recyclerViewGames.setAdapter(gameAdapter);
    recyclerViewGames.addItemDecoration(
        new BaseListDividerDecoration(this, ContextCompat.getColor(this, R.color.grey_divider),
            screenUtils.dpToPx(0.3f)));

    subscriptions.add(gameAdapter.onClick()
        .map(view -> gameAdapter.getItemAtPosition(recyclerViewGames.getChildLayoutPosition(view)))
        .subscribe(game -> onGameSelected(game)));
  }

  private void addFooterItem() {
    GameFooter gameSupport = new GameFooter(this, Game.GAME_SUPPORT);
    items.add(gameSupport);
  }

  protected abstract void onGameSelected(Game game);

  protected abstract int getContentView();

  protected abstract void initDependencyInjector();

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
