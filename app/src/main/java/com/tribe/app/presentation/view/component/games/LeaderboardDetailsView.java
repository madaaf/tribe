package com.tribe.app.presentation.view.component.games;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.GameMVPView;
import com.tribe.app.presentation.view.adapter.decorator.BaseListDividerDecoration;
import com.tribe.app.presentation.view.adapter.manager.LeaderboardDetailsLayoutManager;
import com.tribe.app.presentation.view.adapter.viewholder.LeaderboardDetailsAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TabUnderlinedView;
import com.tribe.tribelivesdk.game.Game;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/09/2017.
 */

public class LeaderboardDetailsView extends ConstraintLayout implements GameMVPView {

  @Inject ScreenUtils screenUtils;

  @Inject User currentUser;

  @Inject LeaderboardDetailsAdapter adapter;

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.recyclerView) RecyclerView recyclerView;

  @BindView(R.id.tabFriends) TabUnderlinedView tabFriends;

  @BindView(R.id.tabOverall) TabUnderlinedView tabOverall;

  @BindView(R.id.viewUnderline) View viewUnderline;

  @BindView(R.id.guidelineHalfWidth) Guideline guidelineHalfWidth;

  // VARIABLES
  private LeaderboardDetailsLayoutManager layoutManager;
  private List<Score> items;
  private Game selectedGame;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LeaderboardDetailsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    items = new ArrayList<>();

    initDependencyInjector();
    initSubscriptions();
    initUI();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();
  }

  private void initUI() {
    layoutManager = new LeaderboardDetailsLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new BaseListDividerDecoration(getContext(),
        ContextCompat.getColor(getContext(), R.color.grey_divider), screenUtils.dpToPx(0.5f)));

    adapter.setItems(items);
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
   * ON CLICK
   */

  @OnClick(R.id.tabFriends) void clickFriends() {
    if (tabFriends.isActive()) return;

    tabFriends.setActive(true);
    tabOverall.setActive(false);

    ConstraintSet set = new ConstraintSet();
    set.clone(this);

    set.connect(viewUnderline.getId(), ConstraintSet.BOTTOM, tabFriends.getId(),
        ConstraintSet.BOTTOM);
    set.connect(viewUnderline.getId(), ConstraintSet.END, guidelineHalfWidth.getId(),
        ConstraintSet.START);
    set.connect(viewUnderline.getId(), ConstraintSet.START, this.getId(), ConstraintSet.START);

    updateConstraints(set);

    gamePresenter.loadGameLeaderboard(selectedGame.getId(), true, 0);
  }

  @OnClick(R.id.tabOverall) void clickOverall() {
    if (tabOverall.isActive()) return;

    tabFriends.setActive(false);
    tabOverall.setActive(true);

    ConstraintSet set = new ConstraintSet();
    set.clone(this);

    set.connect(viewUnderline.getId(), ConstraintSet.BOTTOM, tabOverall.getId(),
        ConstraintSet.BOTTOM);
    set.connect(viewUnderline.getId(), ConstraintSet.START, guidelineHalfWidth.getId(),
        ConstraintSet.END);
    set.connect(viewUnderline.getId(), ConstraintSet.END, this.getId(), ConstraintSet.END);

    updateConstraints(set);

    gamePresenter.loadGameLeaderboard(selectedGame.getId(), false, 0);
  }

  private void updateConstraints(ConstraintSet constraintSet) {
    AutoTransition autoTransition = new AutoTransition();
    autoTransition.setDuration(300);
    TransitionManager.beginDelayedTransition(this, autoTransition);
    constraintSet.applyTo(this);
  }

  @Override public Context context() {
    return null;
  }

  @Override public void onGameLeaderboard(List<Score> scoreList) {

  }

  /**
   * PUBLIC
   */

  public void setGame(Game game) {
    selectedGame = game;
  }

  /**
   * OBSERVABLES
   */
}
