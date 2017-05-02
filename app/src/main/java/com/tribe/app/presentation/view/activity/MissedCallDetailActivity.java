package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.adapter.MissedCallActionAdapter;
import com.tribe.app.presentation.view.adapter.manager.MissedCallLayoutManager;
import com.tribe.app.presentation.view.notification.MissedCallAction;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 27/04/2017.
 */

public class MissedCallDetailActivity extends BaseActivity {

  private static String LIST_MISSED_CALL = "LIST_MISSED_CALL";

  @Inject MissedCallManager missedCallManager;

  @BindView(R.id.recyclerViewMissedCall) RecyclerView recyclerView;

  // VARIABLES
  private Unbinder unbinder;
  private MissedCallLayoutManager layoutManager;
  private MissedCallActionAdapter adapter;
  private List<MissedCallAction> items = new ArrayList<>();

  // OBSERVABLES
  private CompositeSubscription subscriptions;

  public static Intent getIntentForMissedCallDetail(Context context,
      List<MissedCallAction> missedCallAction) {
    Intent intent = new Intent(context, MissedCallDetailActivity.class);
    Bundle extra = new Bundle();
    extra.putSerializable(LIST_MISSED_CALL, (Serializable) missedCallAction);
    intent.putExtras(extra);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail_calls);

    unbinder = ButterKnife.bind(this);
    initDependencyInjector();

    if (getIntent().hasExtra(LIST_MISSED_CALL)) {
      Bundle extra = getIntent().getExtras();
      items.addAll((ArrayList<MissedCallAction>) extra.getSerializable(LIST_MISSED_CALL));
    }
    initView();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build();

    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void initView() {
    subscriptions = new CompositeSubscription();

    layoutManager = new MissedCallLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);

    adapter = new MissedCallActionAdapter(this);
    adapter.setItems(items);

    recyclerView.setAdapter(adapter);

    subscriptions.add(adapter.onHangLive().map(view -> {
      MissedCallAction missedCallAction =
          (MissedCallAction) adapter.getItemAtPosition(recyclerView.getChildLayoutPosition(view));
      return missedCallAction;
    }).subscribe(missedCallAction -> {
      Intent intent =
          NotificationUtils.getIntentForLive(this, missedCallAction.getNotificationPayload());
      startActivity(intent);
      finish();
    }));
  }

  @OnClick(R.id.imgBack) public void onBackClick() {
    onBackPressed();
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
  }
}
