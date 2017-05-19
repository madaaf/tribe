package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.digits.sdk.android.AuthCallback;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.domain.entity.ErrorLogin;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.AuthPresenter;
import com.tribe.app.presentation.mvp.view.AuthMVPView;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class AuthAccessActivity {

/*
  private static final int TIMER_START = 4000;

  public static Intent getCallingIntent(Context context, String countryCode) {
    Intent intent = new Intent(context, AuthAccessActivity.class);
    intent.putExtra(Extras.COUNTRY_CODE, countryCode);
    return intent;
  }

  @Inject User user;

  @Inject AccessPresenter accessPresenter;

  @Inject @AddressBook Preference<Boolean> addressBook;

  @Inject @LastSync Preference<Long> lastSync;

  @BindView(R.id.viewAccess) AccessView viewAccess;

  @BindView(R.id.txtAction) TextViewFont txtAction;

  @BindView(R.id.progressView) CircularProgressView progressView;

  @BindView(R.id.layoutConfettis) FrameLayout layoutConfettis;

  // VARIABLES
  private Uri deepLink;
  private Unbinder unbinder;
  private int totalTimeSynchro;
  private int nbFriends = 0;
  private long timeSyncStart = 0;
  private RxPermissions rxPermissions;
  private String countryCode = null;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Subscription lookupSubscription;
  private Subscription startSubscription;
  private Subscription endSubscription;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth_access);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    init();
    initResources();
    initIntent(getIntent());
    manageDeepLink(getIntent());
  }

  @Override protected void onStart() {
    super.onStart();
    accessPresenter.onViewAttached(this);
  }

  @Override protected void onStop() {
    accessPresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    if (lookupSubscription != null) lookupSubscription.unsubscribe();
    if (startSubscription != null) startSubscription.unsubscribe();
    if (endSubscription != null) endSubscription.unsubscribe();
    super.onDestroy();
  }

  private void init() {
    rxPermissions = new RxPermissions(this);

    startSubscription = Observable.timer(TIMER_START, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> start());

    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_FindFriendsStart);
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  private void initResources() {
    totalTimeSynchro = getResources().getInteger(R.integer.time_synchro);
  }

  private void initIntent(Intent intent) {
    if (intent != null) countryCode = intent.getStringExtra(Extras.COUNTRY_CODE);
  }

  private void manageDeepLink(Intent intent) {
    if (intent != null && intent.getData() != null) {
      deepLink = intent.getData();

      if (deepLink != null && !StringUtils.isEmpty(deepLink.getPath())) {
        if (deepLink.getPath().startsWith("/g/")) {
          accessPresenter.lookupGroupInfos(StringUtils.getLastBitFromUrl(deepLink.toString()));
        }
      }
    }
  }

  @OnClick(R.id.txtAction) void onClickAction() {
    if (viewAccess.getStatus() == AccessView.NONE) {
      tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_FindFriendsNext);
      start();
    } else if (viewAccess.getStatus() == AccessView.LOADING) {
      showCongrats();
    } else if (viewAccess.getStatus() == AccessView.DONE) {
      endSubscription.unsubscribe();
      navigator.navigateToHomeFromLogin(this, deepLink, countryCode);
    }
  }

  @OnClick(R.id.viewAccess) void onClickAccess() {
    if (viewAccess.getStatus() == AccessView.NONE) {
      start();
    }
  }

  @Override public void renderFriendList(List<User> userList) {
    lastSync.set(System.currentTimeMillis());

    Map<String, Object> relationsInApp = new HashMap<>();

    for (User user : userList) {
      relationsInApp.put(user.getId(), user);
    }

    if (relationsInApp.values() != null && relationsInApp.values().size() > 0) {
      viewAccess.animateProgress();

      lookupSubscription = Observable.zip(Observable.from(relationsInApp.values()),
          Observable.interval(0, totalTimeSynchro / relationsInApp.values().size(),
              TimeUnit.MILLISECONDS).onBackpressureDrop(), (contact, aLong) -> contact)
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(relation -> {
            nbFriends++;
            viewAccess.showLoading(nbFriends);

            if (nbFriends == relationsInApp.values().size()) {
              subscriptions.add(Observable.timer(750, TimeUnit.MILLISECONDS)
                  .onBackpressureDrop()
                  .subscribeOn(Schedulers.newThread())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(time -> showCongrats()));
            }
          });
    } else {
      showCongrats();
    }
  }

  @Override public void groupInfosFailed() {

  }

  @Override public void groupInfosSuccess(Group group) {

  }

  private void start() {
    startSubscription.unsubscribe();
    viewAccess.showLoading(0);
    txtAction.setVisibility(View.GONE);
    lookupContacts();
  }

  private void lookupContacts() {
    rxPermissions.request(PermissionUtils.PERMISSIONS_CONTACTS).subscribe(hasPermission -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerUtils.USER_ADDRESS_BOOK_ENABLED, hasPermission);
      tagManager.setProperty(bundle);

      Bundle bundleBis = new Bundle();
      bundleBis.putBoolean(TagManagerUtils.ACCEPTED, true);
      tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_SystemContacts, bundleBis);

      if (hasPermission) {
        addressBook.set(true);
        timeSyncStart = System.currentTimeMillis();
        accessPresenter.lookupContacts();
      } else {
        renderFriendList(new ArrayList<>());
      }
    });
  }

  private void showCongrats() {
    txtAction.setText(R.string.action_next);
    txtAction.setVisibility(View.VISIBLE);

    endSubscription = Observable.timer(TIMER_START, TimeUnit.MILLISECONDS)
        .subscribe(aLong -> navigator.navigateToHomeFromLogin(this, deepLink, countryCode));

    CommonConfetti.rainingConfetti(layoutConfettis, new int[] {
        ContextCompat.getColor(this, R.color.confetti_1),
        ContextCompat.getColor(this, R.color.confetti_2),
        ContextCompat.getColor(this, R.color.confetti_3),
        ContextCompat.getColor(this, R.color.confetti_4),
        ContextCompat.getColor(this, R.color.confetti_5),
        ContextCompat.getColor(this, R.color.confetti_6)
    }).infinite();

    viewAccess.showCongrats();
  }*/
}