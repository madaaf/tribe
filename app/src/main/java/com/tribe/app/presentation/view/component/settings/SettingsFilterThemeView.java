package com.tribe.app.presentation.view.component.settings;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/6/2016.
 */

public class SettingsFilterThemeView extends LinearLayout {

  @Inject User currentUser;

  @BindView(R.id.viewThemeSettings) SettingsThemeView viewThemeSettings;

  @BindView(R.id.viewFilterSettings) SettingsFilterView viewFilterSettings;

  // VARIABLES

  // OBSERVABLES
  private CompositeSubscription subscriptions;

  public SettingsFilterThemeView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    initDependencyInjector();
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    viewFilterSettings.onResume();
  }

  @Override protected void onDetachedFromWindow() {
    viewFilterSettings.onPause();
    super.onDetachedFromWindow();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void init() {
    subscriptions = new CompositeSubscription();

    setOrientation(VERTICAL);
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
