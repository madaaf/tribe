package com.tribe.app.presentation.view.component.games;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Guideline;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
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
import com.tribe.app.presentation.view.adapter.LeaderboardPagerAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TabUnderlinedView;
import com.tribe.tribelivesdk.game.Game;
import javax.inject.Inject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/09/2017.
 */

public class LeaderboardDetailsView extends FrameLayout {

  public static final int LIMIT = 20;

  @Inject ScreenUtils screenUtils;

  @Inject User currentUser;

  //@BindView(R.id.viewPager) ViewPager viewPager;

  @BindView(R.id.viewGameUserCard) GameUserCardView viewGameUserCard;

  @BindView(R.id.cardView) CardView cardView;

  //@BindView(R.id.tabFriends) TabUnderlinedView tabFriends;

  //@BindView(R.id.tabOverall) TabUnderlinedView tabOverall;

  //@BindView(R.id.viewUnderline) View viewUnderline;

  //@BindView(R.id.guidelineHalfWidth) Guideline guidelineHalfWidth;

  // VARIABLES
  private LeaderboardPagerAdapter adapter;
  private Game selectedGame;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Score> onViewCardClick = PublishSubject.create();

  public LeaderboardDetailsView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    initDependencyInjector();
    initSubscriptions();
    initPresenter();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();
  }

  private void initPresenter() {

  }

  private void initUI() {
    LeaderboardPage leaderboardPage =
        new LeaderboardPage(getContext(), true, selectedGame);
    leaderboardPage.initViewCardClickObservable(onViewCardClick);
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    addView(leaderboardPage, params);
    //adapter = new LeaderboardPagerAdapter(getContext(), selectedGame, onViewCardClick);
    //viewPager.setAdapter(adapter);
    //
    //viewPager.setOffscreenPageLimit(2);
    //viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
    //  @Override
    //  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    //
    //  }
    //
    //  @Override public void onPageSelected(int position) {
    //    if (position == 0) {
    //      setFriends();
    //    } else {
    //      setOverall();
    //    }
    //  }
    //
    //  @Override public void onPageScrollStateChanged(int state) {
    //
    //  }
    //});

    cardView.setTranslationY(screenUtils.getHeightPx() >> 1);

    Score score = currentUser.getScoreForGame(selectedGame.getId());

    if (score == null) {
      score = new Score();
      score.setUser(currentUser);
    }

    viewGameUserCard.setOnClickListener(v -> onViewCardClick.onNext(currentUser.getScoreForGame(selectedGame.getId())));
    viewGameUserCard.setScore(score);
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

  //@OnClick(R.id.tabFriends) void clickFriends() {
  //  viewPager.setCurrentItem(0, true);
  //}

  //@OnClick(R.id.tabOverall) void clickOverall() {
  //  viewPager.setCurrentItem(1, true);
  //}

  /*private void setFriends() {
    if (tabFriends.isActive()) return;

    hideGameCard();

    tabFriends.setActive(true);
    tabOverall.setActive(false);

    ConstraintSet set = new ConstraintSet();
    set.clone(this);

    set.connect(viewUnderline.getBirdId(), ConstraintSet.BOTTOM, tabFriends.getBirdId(),
        ConstraintSet.BOTTOM);
    set.connect(viewUnderline.getBirdId(), ConstraintSet.END, guidelineHalfWidth.getBirdId(),
        ConstraintSet.START);
    set.connect(viewUnderline.getBirdId(), ConstraintSet.START, this.getBirdId(), ConstraintSet.START);

    updateConstraints(set);
  }*/

  //private void setOverall() {
  //  if (tabOverall.isActive()) return;
  //
  //  showGameCard();
  //
  //  tabFriends.setActive(false);
  //  tabOverall.setActive(true);
  //
  //  ConstraintSet set = new ConstraintSet();
  //  set.clone(this);
  //
  //  set.connect(viewUnderline.getBirdId(), ConstraintSet.BOTTOM, tabOverall.getBirdId(),
  //      ConstraintSet.BOTTOM);
  //  set.connect(viewUnderline.getBirdId(), ConstraintSet.START, guidelineHalfWidth.getBirdId(),
  //      ConstraintSet.END);
  //  set.connect(viewUnderline.getBirdId(), ConstraintSet.END, this.getBirdId(), ConstraintSet.END);
  //
  //  updateConstraints(set);
  //}
  //
  //private void updateConstraints(ConstraintSet constraintSet) {
  //  AutoTransition autoTransition = new AutoTransition();
  //  autoTransition.setDuration(100);
  //  TransitionManager.beginDelayedTransition(this, autoTransition);
  //  constraintSet.applyTo(this);
  //}

  private void hideGameCard() {
    cardView.animate()
        .setDuration(300)
        .setInterpolator(new DecelerateInterpolator())
        .translationY(screenUtils.getHeightPx() / 4)
        .start();
  }

  private void showGameCard() {
    cardView.animate()
        .setDuration(300)
        .setInterpolator(new DecelerateInterpolator())
        .translationY(0)
        .start();
  }

  /**
   * PUBLIC
   */

  public void setGame(Game game) {
    selectedGame = game;
    initUI();
  }

  /**
   * OBSERVABLES
   */
}
