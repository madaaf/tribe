package com.tribe.app.presentation.view.component.settings;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.component.ProfileInfoView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/28/2016.
 */

public class SettingsProfileView extends FrameLayout {

  @Inject User user;

  @Inject Navigator navigator;

  @BindView(R.id.viewInfoProfile) ProfileInfoView viewInfoProfile;

  // VARIABLES

  // OBSERVABLES
  private CompositeSubscription subscriptions;

  public SettingsProfileView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

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
  }

  public String getUsername() {
    return viewInfoProfile.getUsername();
  }

  public String getDisplayName() {
    return viewInfoProfile.getDisplayName();
  }

  public String getImgUri() {
    return viewInfoProfile.getImgUri();
  }

  public void setUsernameValid(boolean valid) {
    viewInfoProfile.setUsernameValid(valid);
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

  public Observable<Boolean> onInfoValid() {
    return viewInfoProfile.onInfoValid();
  }

  public Observable<String> onUsernameInput() {
    return viewInfoProfile.onUsernameInput();
  }
}
