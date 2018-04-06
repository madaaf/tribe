package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.DebugPresenter;
import com.tribe.app.presentation.mvp.view.DebugMVPView;
import com.tribe.app.presentation.utils.preferences.NewWS;
import com.tribe.app.presentation.utils.preferences.RoutingMode;
import com.tribe.app.presentation.utils.preferences.TribeState;
import com.tribe.app.presentation.utils.preferences.Walkthrough;
import com.tribe.app.presentation.utils.preferences.WebSocketUrlOverride;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/04/2016
 */
public class DebugActivity extends BaseActivity implements DebugMVPView {

  public static Intent getCallingIntent(Context context) {
    return new Intent(context, DebugActivity.class);
  }

  @Inject UserCache userCache;

  @Inject AccessToken accessToken;

  @Inject DebugPresenter debugPresenter;

  @Inject @RoutingMode Preference<String> routingMode;

  @Inject @NewWS Preference<Boolean> newWS;

  @Inject @TribeState Preference<Set<String>> tutorialState;

  @Inject @Walkthrough Preference<Boolean> walkthrough;

  @Inject @WebSocketUrlOverride Preference<String> webSocketUrl;

  @BindView(R.id.viewActionRouted) ActionView viewActionRouted;

  @BindView(R.id.viewActionFuckUpSomeTokens) ActionView viewActionFuckUpSomeTokens;

  @BindView(R.id.viewActionTooltip) ActionView viewActionTooltip;

  @BindView(R.id.viewActionSync) ActionView viewActionSync;

  @BindView(R.id.viewActionNewWS) ActionView viewActionNewWS;

  @BindView(R.id.txtAction) TextViewFont txtAction;

  @BindView(R.id.editTxtWebsocket) EditText editTextWebsocket;

  private Unbinder unbinder;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initDependencyInjector();
    initUI();
    initPresenter();
    initSubscriptions();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions != null) subscriptions.unsubscribe();
    super.onDestroy();
  }

  private void initUI() {
    setContentView(R.layout.activity_debug);
    unbinder = ButterKnife.bind(this);

    viewActionRouted.setTitle("Routed mode");
    viewActionRouted.setValue(routingMode.get().equals(TribeLiveOptions.ROUTED));
    viewActionFuckUpSomeTokens.setTitle("Fuck up some tokens");
    viewActionTooltip.setTitle("Clear tooltip");
    viewActionSync.setTitle("Sync");
    viewActionNewWS.setTitle("New Websocket");
    viewActionNewWS.setValue(newWS.get());
    editTextWebsocket.setText(webSocketUrl.get());
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

  private void initSubscriptions() {
    subscriptions.add(viewActionRouted.onChecked().subscribe(aBoolean -> {
      routingMode.set(aBoolean ? TribeLiveOptions.ROUTED : TribeLiveOptions.P2P);
    }));

    subscriptions.add(viewActionNewWS.onChecked().subscribe(aBoolean -> newWS.set(aBoolean)));

    subscriptions.add(viewActionFuckUpSomeTokens.onClick().subscribe(aVoid -> {
      accessToken.setAccessToken("thisisafaketokenfortestingpurposestiago");
      userCache.put(accessToken);
    }));

    subscriptions.add(viewActionTooltip.onClick().subscribe(aVoid -> {
      walkthrough.set(false);
      tutorialState.set(new HashSet<>());
    }));

    subscriptions.add(viewActionSync.onClick().subscribe(aVoid -> {
      viewActionSync.setBody("Syncing...");
      debugPresenter.lookupContacts(this);
    }));
  }

  @OnClick(R.id.txtAction) void save() {
    webSocketUrl.set(editTextWebsocket.getText().toString());
    finish();
  }

  @OnClick(R.id.btnBack) void back() {
    finish();
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  @Override public void onSyncDone() {
    viewActionSync.setBody("Done!");
  }
}
