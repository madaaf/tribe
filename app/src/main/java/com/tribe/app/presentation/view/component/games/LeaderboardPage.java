package com.tribe.app.presentation.view.component.games;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.view.adapter.decorator.BaseListDividerDecoration;
import com.tribe.app.presentation.view.adapter.manager.LeaderboardDetailsLayoutManager;
import com.tribe.app.presentation.view.adapter.viewholder.LeaderboardDetailsAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.game.Game;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/09/2017.
 */
public class LeaderboardPage extends LinearLayout {

  public static final int LIMIT = 20;

  @Inject GamePresenter gamePresenter;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  // VARIABLES
  private LeaderboardDetailsLayoutManager layoutManager;
  private LeaderboardDetailsAdapter adapter;
  private List<Object> items;
  private Game selectedGame;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private boolean friends = false;

  // DIMENS

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LeaderboardPage(Context context, boolean friends, Game selectedGame) {
    super(context);
    this.friends = friends;
    this.selectedGame = selectedGame;
    init();
  }

  public LeaderboardPage(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    LayoutInflater.from(getContext()).inflate(R.layout.item_page_leaderboard, this);
    unbinder = ButterKnife.bind(this);

    items = new ArrayList<>();

    initResources();
    initDependencyInjector();
    initUI();
    initPresenter();
    initSubscriptions();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    gamePresenter.onViewDetached();
    subscriptions.clear();
  }

  private void initUI() {
    layoutManager = new LeaderboardDetailsLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    adapter = new LeaderboardDetailsAdapter(getContext(), recyclerView);
    recyclerView.setItemAnimator(null);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new BaseListDividerDecoration(getContext(),
        ContextCompat.getColor(getContext(), R.color.grey_divider), screenUtils.dpToPx(0.5f)));

    adapter.setItems(items);

    //subscriptions.add(adapter.onLoadMore().subscribe(downwards -> {
    //  if (!downwards && friends) return;
    //
    //  if (downwards) {
    //    if (items.get(items.size() - 1) instanceof Score) {
    //      Score score = (Score) items.get(items.size() - 1);
    //      gamePresenter.load(selectedGame.getId(), friends, false, LIMIT,
    //          score.getRanking() + 1, downwards);
    //    }
    //  } else {
    //    if (items.get(0) instanceof Score) {
    //      Score score = (Score) items.get(0);
    //      if (score.getRanking() == 0 || score.getRanking() == 1) return;
    //      int offset = score.getRanking() - LIMIT;
    //      int limit = offset < 0 ? LIMIT + offset : LIMIT;
    //      gamePresenter.loadGameLeaderboard(selectedGame.getId(), friends, false, limit,
    //          offset < 0 ? 0 : offset, downwards);
    //    }
    //  }
    //}));

    gamePresenter.loadFriendsScore(selectedGame.getId());
  }

  private void initPresenter() {
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public Context context() {
        return getContext();
      }

      @Override
      public void onGameLeaderboard(List<Score> scoreList, boolean cloud, boolean friendsOnly,
          int offset, boolean downwards) {
        // NOT USED FOR NOW
        //if (cloud) {
        //  if (!downwards) {
        //    items.addAll(0, scoreList);
        //    adapter.addItems(0, scoreList);
        //  } else {
        //    items.addAll(scoreList);
        //    adapter.addItems(scoreList);
        //  }
        //} else {
        //  adapter.setItems(items);
        //}
      }

      @Override public void onFriendsScore(List<Score> scoreList, boolean cloud) {
        Collections.sort(scoreList, (o1, o2) -> ((Integer) o2.getValue()).compareTo(o1.getValue()));
        items.clear();
        items.addAll(scoreList);
        adapter.setItems(items);
      }
    };
  }

  private void initResources() {

  }

  private void initSubscriptions() {

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

  ///////////////////////
  //      PUBLIC       //
  ///////////////////////

  public void initViewCardClickObservable(Observable<Score> scoreObs) {
    if (friends) return;
    subscriptions.add(scoreObs.subscribe(score -> {
      items.clear();
      adapter.clear();
      gamePresenter.loadGameLeaderboard(selectedGame.getId(), friends, false, LIMIT,
          score.getRanking(), true);
    }));
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////
}