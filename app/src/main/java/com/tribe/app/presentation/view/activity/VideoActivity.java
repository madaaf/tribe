package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.component.onboarding.AuthVideoView;
import rx.subscriptions.CompositeSubscription;

public class VideoActivity extends BaseActivity {

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, VideoActivity.class);
    return intent;
  }

  @BindView(R.id.viewVideoAuth) AuthVideoView authVideoView;

  // VARIABLES
  private Unbinder unbinder;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_video);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    init();
  }

  @Override protected void onPause() {
    authVideoView.onPause(true);
    super.onPause();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  private void init() {
    subscriptions.add(authVideoView.videoCompleted().subscribe(aBoolean -> finish()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }
}