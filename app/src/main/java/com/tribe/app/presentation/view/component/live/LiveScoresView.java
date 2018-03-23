package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.View;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class LiveScoresView extends ConstraintLayout {

  @Inject ScreenUtils screenUtils;

  // VARIABLES
  private LiveRowViewAddFriend liveRowViewAddFriend;
  private Map<String, LiveRowViewScores> mapViews;
  private Map<String, Subscription> mapViewsScoreChangeSubscription;

  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveScoresView(Context context) {
    super(context);
    init();
  }

  public LiveScoresView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void dispose() {
    for (Subscription subscription : mapViewsScoreChangeSubscription.values())
      subscription.unsubscribe();
    mapViewsScoreChangeSubscription.clear();
    for (View v : mapViews.values()) {
      if (v.getParent() != null) removeView(v);
    }
    mapViews.clear();
    removeAllViews();
    removeView(liveRowViewAddFriend);
  }

  private void init() {
    initDependencyInjector();

    mapViewsScoreChangeSubscription = new HashMap<>();
    mapViews = new HashMap<>();

    liveRowViewAddFriend = new LiveRowViewAddFriend(getContext());
    liveRowViewAddFriend.setId(View.generateViewId());

    addView(liveRowViewAddFriend);
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

  private void refactorConstraintsOnChilds() {
    int lastStream = 0;

    ConstraintSet set = new ConstraintSet();
    set.clone(this);

    LiveRowViewScores v;

    List<LiveRowViewScores> views = new ArrayList<>(mapViews.values());
    Collections.sort(views, (o1, o2) -> new Integer(o2.getScore()).compareTo(o1.getScore()));

    for (int i = 0; i < views.size(); i++) {
      v = views.get(i);

      if (v != null) {
        set.clear(v.getId());

        set.connect(v.getId(), ConstraintSet.START, getId(), ConstraintSet.START,
            screenUtils.dpToPx(20));
        set.constrainWidth(v.getId(), screenUtils.dpToPx(150));
        set.constrainHeight(v.getId(), screenUtils.dpToPx(LiveStreamView.MAX_HEIGHT_LIST));
        set.setElevation(v.getId(), screenUtils.dpToPx(10));

        switch (i) {
          case 0:
            set.connect(v.getId(), ConstraintSet.TOP, getId(), ConstraintSet.TOP,
                screenUtils.dpToPx(15));
            break;

          default:
            View previous = views.get(lastStream);
            set.connect(v.getId(), ConstraintSet.TOP, previous.getId(), ConstraintSet.BOTTOM,
                screenUtils.dpToPx(15));
            break;
        }

        lastStream = i;
      }
    }

    View previous = views.get(lastStream);
    set.connect(liveRowViewAddFriend.getId(), ConstraintSet.TOP, previous.getId(),
        ConstraintSet.BOTTOM, screenUtils.dpToPx(15));
    set.connect(liveRowViewAddFriend.getId(), ConstraintSet.START, getId(), ConstraintSet.START,
        screenUtils.dpToPx(20));
    set.constrainWidth(liveRowViewAddFriend.getId(),
        screenUtils.dpToPx(LiveStreamView.MAX_HEIGHT_LIST));
    set.constrainHeight(liveRowViewAddFriend.getId(),
        screenUtils.dpToPx(LiveStreamView.MAX_HEIGHT_LIST));

    AutoTransition autoTransition = new AutoTransition();
    TransitionManager.beginDelayedTransition(this, autoTransition);
    set.applyTo(this);
  }

  /**
   * PUBLIC
   */

  public void add(LiveRowViewScores view) {
    String userId = view.getGuest().getId();
    mapViews.put(userId, view);
    mapViewsScoreChangeSubscription.put(userId,
        view.onScoreChange().subscribe(pair -> refactorConstraintsOnChilds()));
    view.setId(View.generateViewId());
    addView(view);
    refactorConstraintsOnChilds();
  }

  public void remove(LiveRowViewScores view) {
    String userId = view.getGuest().getId();
    mapViews.remove(view.getGuest().getId());
    mapViewsScoreChangeSubscription.get(userId).unsubscribe();
    mapViewsScoreChangeSubscription.remove(userId);
    removeView(view);
    refactorConstraintsOnChilds();
  }

  /**
   * OBSERVABLES
   */

  public Observable<Void> onClick() {
    return liveRowViewAddFriend.onClick();
  }
}