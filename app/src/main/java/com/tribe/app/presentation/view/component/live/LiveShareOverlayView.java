package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 04/29/17.
 */
public class LiveShareOverlayView extends LinearLayout {

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.btnShare) View btnShare;

  // VARIABLES
  private Unbinder unbinder;

  // RESOURCES

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onShare = PublishSubject.create();

  public LiveShareOverlayView(Context context) {
    super(context);
    init();
  }

  public LiveShareOverlayView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveShareOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_share_overlay, this);
    unbinder = ButterKnife.bind(this);

    setBackgroundResource(R.color.black_opacity_40);
    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER);
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

  ////////////
  // PUBLIC //
  ////////////

  public void show() {
    setVisibility(View.VISIBLE);
  }

  public void hide() {
    setVisibility(View.GONE);
  }

  ////////////
  // CLICKS //
  ////////////

  @OnClick(R.id.btnShare) void share() {
    onShare.onNext(null);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Void> onShare() {
    return onShare;
  }
}
