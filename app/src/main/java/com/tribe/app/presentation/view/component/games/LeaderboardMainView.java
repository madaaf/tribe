package com.tribe.app.presentation.view.component.games;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.adapter.manager.LeaderboardGeneralLayoutManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/07/2017.
 */

public class LeaderboardMainView extends FrameLayout {

  @Inject ScreenUtils screenUtils;

  @Inject User currentUser;

  @BindView(R.id.recyclerViewGames) RecyclerView recyclerViewGames;

  // VARIABLES
  private LeaderboardGeneralLayoutManager layoutManager;
  private List<Object> items;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LeaderboardMainView(Context context, AttributeSet attrs) {
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
    layoutManager = new LeaderboardGeneralLayoutManager(getContext());
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
   * OBSERVABLES
   */
}
