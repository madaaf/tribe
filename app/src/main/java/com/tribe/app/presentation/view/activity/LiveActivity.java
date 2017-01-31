package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.LivePresenter;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.view.component.live.LiveContainer;
import com.tribe.app.presentation.view.component.live.LiveInviteView;
import com.tribe.app.presentation.view.component.live.LiveView;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class LiveActivity extends BaseActivity implements LiveMVPView {

  private static final String EXTRA_RECIPIENT = "EXTRA_RECIPIENT";

  public static Intent getCallingIntent(Context context, Recipient recipient) {
    Intent intent = new Intent(context, LiveActivity.class);
    intent.putExtra(EXTRA_RECIPIENT, recipient);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @Inject LivePresenter livePresenter;

  @BindView(R.id.viewLive) LiveView viewLive;

  @BindView(R.id.viewInviteLive) LiveInviteView viewInviteLive;

  @BindView(R.id.viewLiveContainer) LiveContainer viewLiveContainer;

  // VARIABLES
  private Unbinder unbinder;
  private Recipient recipient;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_live);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    if (getIntent().hasExtra(EXTRA_RECIPIENT)) initParams();
    init();
    initResources();
    initPermissions();
    initSubscriptions();
  }

  @Override protected void onStart() {
    super.onStart();
    livePresenter.onViewAttached(this);
  }

  @Override protected void onStop() {
    livePresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onResume() {
    super.onResume();
  }

  @Override protected void onPause() {
    super.onPause();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  private void initParams() {
    this.recipient = (Recipient) getIntent().getSerializableExtra(EXTRA_RECIPIENT);
  }

  private void init() {
    ViewGroup.LayoutParams params = viewInviteLive.getLayoutParams();
    params.width = screenUtils.getWidthPx() / 3;
    viewInviteLive.setLayoutParams(params);
    viewInviteLive.requestLayout();

    viewLive.setRecipient(recipient);
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  private void initResources() {
    int result = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

    if (resourceId > 0) {
      result = getResources().getDimensionPixelSize(resourceId);
    }

    viewLiveContainer.setStatusBarHeight(result);
  }

  private void initPermissions() {
    subscriptions.add(RxPermissions.getInstance(LiveActivity.this)
        .request(PermissionUtils.PERMISSIONS_LIVE)
        .subscribe(granted -> {

        }));
  }

  private void initSubscriptions() {
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  ////////////////
  //   PUBLIC   //
  ////////////////

  @Override public void renderFriendshipList(List<Friendship> friendshipList) {
    viewInviteLive.renderFriendshipList(friendshipList);
  }
}