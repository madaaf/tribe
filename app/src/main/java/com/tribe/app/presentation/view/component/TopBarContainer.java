package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.SoundManager;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class TopBarContainer extends FrameLayout {

  @Inject SoundManager soundManager;

  @BindView(R.id.recyclerViewFriends) RecyclerView recyclerView;

  @BindView(R.id.topBarView) TopBarView topBarView;

  // VARIABLES

  // DIMENS

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public TopBarContainer(Context context) {
    super(context);
  }

  public TopBarContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {

    unbinder.unbind();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }

    super.onDetachedFromWindow();
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    unbinder = ButterKnife.bind(this);

    ApplicationComponent applicationComponent =
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
    applicationComponent.inject(this);

    initDimen();
    initUI();
    initSubscriptions();
  }

  private void initUI() {
    recyclerView.setOnTouchListener((v, event) -> {
      int dy = recyclerView.computeVerticalScrollOffset();
      if (event.getY() < topBarView.getHeight() && dy < (topBarView.getHeight() >> 1)) return true;

      return super.onTouchEvent(event);
    });
  }

  private void initDimen() {

  }

  private void initSubscriptions() {

  }

  public boolean isSearchMode() {
    return topBarView.isSearchMode();
  }

  public void closeSearch() {
    topBarView.closeSearch();
  }

  ///////////////////////
  //    TOUCH EVENTS   //
  ///////////////////////

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    boolean isTouchInTopBar = ev.getRawY() < topBarView.getHeight();
    if (!isEnabled() || canChildScrollUp() || isTouchInTopBar) {
      if (isTouchInTopBar) topBarView.onTouchEvent(ev);

      return false;
    }

    return false;
  }

  private boolean canChildScrollUp() {
    return ViewCompat.canScrollVertically(recyclerView, -1);
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////

  public Observable<Void> onClickNew() {
    return topBarView.onClickNew();
  }

  public Observable<String> onSearch() {
    return topBarView.onSearch();
  }

  public Observable<Void> onClickProfile() {
    return topBarView.onClickProfile();
  }

  public Observable<Void> onClickInvite() {
    return topBarView.onClickInvite();
  }

  public Observable<Boolean> onOpenCloseSearch() {
    return topBarView.onOpenCloseSearch();
  }
}