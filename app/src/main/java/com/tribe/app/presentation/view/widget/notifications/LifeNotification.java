package com.tribe.app.presentation.view.widget.notifications;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 10/04/2017.
 */

public class LifeNotification extends FrameLayout {

  @Inject StateManager stateManager;
  @Inject ScreenUtils screenUtils;
  @Inject Navigator navigator;
  @Inject TagManager tagManager;

  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected PublishSubject<Void> onHideNotification = PublishSubject.create();

  public LifeNotification(@NonNull Context context) {
    super(context);
  }

  public LifeNotification(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.clear();
    clearAnimation();
    super.onDetachedFromWindow();
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  protected void hideView() {
    onHideNotification.onNext(null);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Void> onHideNotification() {
    return onHideNotification;
  }
}
