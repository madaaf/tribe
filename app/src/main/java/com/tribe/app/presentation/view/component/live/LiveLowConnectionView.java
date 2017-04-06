package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveLowConnectionView extends FrameLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.viewLiveWave) View viewLiveWave;

  // RESOURCES

  // VARIABLES
  private Unbinder unbinder;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveLowConnectionView(Context context) {
    super(context);
    init(context, null);
  }

  public LiveLowConnectionView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public LiveLowConnectionView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();

    super.onDetachedFromWindow();
  }

  private void init(Context context, AttributeSet attrs) {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_low_connectivity, this);
    unbinder = ButterKnife.bind(this);

    setBackgroundResource(R.color.black_opacity_40);
  }

  private void initResources() {

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

  //////////////
  //  PUBLIC  //
  //////////////

  /////////////////
  // OBSERVABLES //
  /////////////////
}
