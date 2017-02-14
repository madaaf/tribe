package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.DebugPresenter;
import com.tribe.app.presentation.mvp.view.DebugMVPView;
import javax.inject.Inject;

/**
 * Created by tiago on 11/04/2016
 */
public class DebugActivity extends BaseActivity implements DebugMVPView {

  public static Intent getCallingIntent(Context context) {
    return new Intent(context, DebugActivity.class);
  }

  @Inject DebugPresenter debugPresenter;

  private Unbinder unbinder;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initUI();
    initDependencyInjector();
    initPresenter();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    super.onDestroy();
  }

  private void initUI() {
    setContentView(R.layout.activity_debug);
    unbinder = ButterKnife.bind(this);
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  private void initPresenter() {
    this.debugPresenter.onViewAttached(this);
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }
}
