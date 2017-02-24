package com.tribe.app.presentation.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.f2prateek.rx.preferences.Preference;
import com.facebook.AccessToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.data.network.WSService;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.scope.HasComponent;
import com.tribe.app.presentation.mvp.presenter.HomeGridPresenter;
import com.tribe.app.presentation.mvp.view.HomeGridMVPView;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.view.adapter.HomeGridAdapter;
import com.tribe.app.presentation.view.adapter.diff.GridDiffCallback;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.component.TopBarContainer;
import com.tribe.app.presentation.view.component.home.SearchView;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ListUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends BaseActivity
    implements HasComponent<UserComponent>, HomeGridMVPView,
    GoogleApiClient.OnConnectionFailedListener {

  private static final long TWENTY_FOUR_HOURS = 86400000;
  public static final int SETTINGS_RESULT = 101;

  public static Intent getCallingIntent(Context context) {
    return new Intent(context, HomeActivity.class);
  }

  @Inject NotificationManagerCompat notificationManager;

  @Inject HomeGridPresenter homeGridPresenter;

  @Inject HomeGridAdapter homeGridAdapter;

  @Inject ScreenUtils screenUtils;

  @Inject PaletteGrid paletteGrid;

  @Inject StateManager stateManager;

  @Inject @AddressBook Preference<Boolean> addressBook;

  @Inject @LastVersionCode Preference<Integer> lastVersion;

  @Inject @LastSync Preference<Long> lastSync;

  @Inject SoundManager soundManager;

  @BindView(R.id.recyclerViewFriends) RecyclerView recyclerViewFriends;

  @BindView(android.R.id.content) ViewGroup rootView;

  @BindView(R.id.topBarContainer) TopBarContainer topBarContainer;

  @BindView(R.id.searchView) SearchView searchView;

  // OBSERVABLES
  private UserComponent userComponent;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Scheduler singleThreadExecutor;
  private PublishSubject<List<Recipient>> onRecipientUpdates = PublishSubject.create();

  // VARIABLES
  private HomeLayoutManager layoutManager;
  private List<Recipient> latestRecipientList;
  private boolean shouldOverridePendingTransactions = false;
  private NotificationReceiver notificationReceiver;
  private boolean receiverRegistered = false;
  private boolean hasSynced = false;
  private boolean canEndRefresh = false;

  // DIMEN

  @Override protected void onCreate(Bundle savedInstanceState) {
    getWindow().setBackgroundDrawableResource(android.R.color.black);
    super.onCreate(savedInstanceState);

    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_HomeScreen);

    initDependencyInjector();
    init();
    initUi();
    initDimensions();
    initRegistrationToken();
    initRecyclerView();
    initTopBar();
    initSearch();
    manageDeepLink(getIntent());
    initPullToRefresh();

    homeGridPresenter.onViewAttached(this);
    homeGridPresenter.reload(hasSynced);
    if (!hasSynced) hasSynced = true;

    subscriptions.add(Observable.
        from(PermissionUtils.PERMISSIONS_CAMERA)
        .map(permission -> RxPermissions.getInstance(HomeActivity.this).isGranted(permission))
        .toList()
        .subscribe(grantedList -> {
          boolean areAllGranted = true;

          for (Boolean granted : grantedList) {
            if (!granted) areAllGranted = false;
          }

          if (areAllGranted) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(TagManagerUtils.USER_CAMERA_ENABLED, areAllGranted);
            bundle.putBoolean(TagManagerUtils.USER_MICROPHONE_ENABLED, areAllGranted);
            tagManager.setProperty(bundle);

            Bundle bundleBis = new Bundle();
            bundleBis.putBoolean(TagManagerUtils.ACCEPTED, true);
            tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_SystemCamera, bundleBis);
            tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_SystemMicrophone, bundleBis);
          }
        }));

    startService(WSService.getCallingIntent(this));
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    manageDeepLink(intent);
  }

  @Override protected void onStart() {
    super.onStart();
    tagManager.onStart(this);

    if (System.currentTimeMillis() - lastSync.get() > TWENTY_FOUR_HOURS) {
      //homeGridPresenter.syncFriendList();
      syncContacts();
    }
  }

  @Override protected void onRestart() {
    super.onRestart();

    notificationManager.cancelAll();
  }

  @Override protected void onStop() {
    tagManager.onStop(this);
    super.onStop();
  }

  @Override protected void onResume() {
    super.onResume();

    if (shouldOverridePendingTransactions) {
      overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
      shouldOverridePendingTransactions = false;
    }

    if (!receiverRegistered) {
      if (notificationReceiver == null) notificationReceiver = new NotificationReceiver();

      registerReceiver(notificationReceiver,
          new IntentFilter(BroadcastUtils.BROADCAST_NOTIFICATIONS));
      receiverRegistered = true;
    }
  }

  @Override protected void onPause() {
    if (receiverRegistered) {
      unregisterReceiver(notificationReceiver);
      receiverRegistered = false;
    }

    super.onPause();
  }

  @Override protected void onDestroy() {
    recyclerViewFriends.setAdapter(null);

    homeGridPresenter.onViewDetached();

    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();

    Intent i = new Intent(this, WSService.class);
    stopService(i);
    super.onDestroy();
  }

  private void init() {
    singleThreadExecutor = Schedulers.from(Executors.newSingleThreadExecutor());
    latestRecipientList = new ArrayList<>();
  }

  private void initUi() {
    setContentView(R.layout.activity_home);
    ButterKnife.bind(this);
  }

  private void initDimensions() {
  }

  private void initPullToRefresh() {
    subscriptions.add(topBarContainer.onRefresh()
        .doOnNext(aVoid -> {
          canEndRefresh = true;
        })
        .doOnError(throwable -> throwable.printStackTrace())
        .delay(TopBarContainer.MIN_LENGTH, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aVoid -> {
          if (canEndRefresh) {
            homeGridPresenter.reload(false);
            canEndRefresh = false;
          }
        }));
  }

  private void initRecyclerView() {
    layoutManager = new HomeLayoutManager(context());
    layoutManager.setAutoMeasureEnabled(false);
    recyclerViewFriends.setLayoutManager(layoutManager);
    recyclerViewFriends.setItemAnimator(null);
    homeGridAdapter.setItems(new ArrayList<>());
    recyclerViewFriends.setAdapter(homeGridAdapter);

    // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(0, 50);
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(1, 50);
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(2, 50);
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(3, 50);

    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override public int getSpanSize(int position) {
        switch (homeGridAdapter.getItemViewType(position)) {
          case HomeGridAdapter.EMPTY_HEADER_VIEW_TYPE:
            return layoutManager.getSpanCount();
          default:
            return 1;
        }
      }
    });

    subscriptions.add(Observable.merge(homeGridAdapter.onClickMore(), homeGridAdapter.onLongClick())
        .map(view -> homeGridAdapter.getItemAtPosition(
            recyclerViewFriends.getChildLayoutPosition(view)))
        .flatMap(recipient -> {
          if (recipient instanceof Membership) {
            Membership membership = (Membership) recipient;
            navigator.navigateToGroupDetails(this, membership);
            return Observable.empty();
          } else {
            return DialogFactory.showBottomSheetForRecipient(this, recipient);
          }
        }, ((recipient, labelType) -> {
          if (labelType != null) {
            if (labelType.getTypeDef().equals(LabelType.HIDE) || labelType.getTypeDef()
                .equals(LabelType.BLOCK_HIDE)) {
              Friendship friendship = (Friendship) recipient;
              homeGridPresenter.updateFriendship(friendship.getId(), friendship.isMute(),
                  labelType.getTypeDef().equals(LabelType.BLOCK_HIDE) ? FriendshipRealm.BLOCKED
                      : FriendshipRealm.HIDDEN);
            } else if (labelType.getTypeDef().equals(LabelType.MUTE)) {
              Friendship friendship = (Friendship) recipient;
              friendship.setMute(true);
              homeGridPresenter.updateFriendship(friendship.getId(), true, friendship.getStatus());
            } else if (labelType.getTypeDef().equals(LabelType.UNMUTE)) {
              Friendship friendship = (Friendship) recipient;
              friendship.setMute(false);
              homeGridPresenter.updateFriendship(friendship.getId(), false, friendship.getStatus());
            } else if (labelType.getTypeDef().equals(LabelType.GROUP_INFO)) {
              Membership membership = (Membership) recipient;
              navigator.navigateToGroupDetails(this, membership);
            } else if (labelType.getTypeDef().equals(LabelType.GROUP_LEAVE)) {
              homeGridPresenter.leaveGroup(recipient.getId());
            }
          }

          return recipient;
        }))
        .subscribe());

    subscriptions.add(homeGridAdapter.onClick()
        .map(view -> homeGridAdapter.getItemAtPosition(
            recyclerViewFriends.getChildLayoutPosition(view)))
        .subscribe(recipient -> {
          if (stateManager.shouldDisplay(StateManager.ENTER_FIRST_LIVE)) {
            subscriptions.add(
                DialogFactory.dialog(this, getString(R.string.tips_enterfirstlive_title),
                    getString(R.string.tips_enterfirstlive_message, recipient.getDisplayName()),
                    getString(R.string.tips_enterfirstlive_action1),
                    getString(R.string.tips_enterfirstlive_action2))
                    .filter(x -> x == true)
                    .subscribe(a -> {
                      navigator.navigateToLive(this, recipient,
                          PaletteGrid.get(recipient.getPosition()));
                    }));
            stateManager.addTutorialKey(StateManager.ENTER_FIRST_LIVE);
          } else {
            navigator.navigateToLive(this, recipient, PaletteGrid.get(recipient.getPosition()));
          }
        }));

    subscriptions.add(onRecipientUpdates.onBackpressureBuffer().subscribeOn(singleThreadExecutor).
        map(recipientList -> {
          DiffUtil.DiffResult diffResult = null;
          List<Recipient> temp = new ArrayList<>();
          temp.add(new Friendship(Recipient.ID_HEADER));
          temp.addAll(recipientList);
          ListUtils.addEmptyItems(screenUtils, temp);

          if (latestRecipientList.size() != 0) {
            diffResult = DiffUtil.calculateDiff(new GridDiffCallback(latestRecipientList, temp));
            homeGridAdapter.setItems(temp);
          }

          latestRecipientList.clear();
          latestRecipientList.addAll(temp);
          return diffResult;
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(diffResult -> {
      if (diffResult != null) {
        diffResult.dispatchUpdatesTo(homeGridAdapter);
      } else {
        homeGridAdapter.setItems(latestRecipientList);
        homeGridAdapter.notifyDataSetChanged();
      }
    }));
  }

  private void initDependencyInjector() {
    this.userComponent = DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build();

    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void initTopBar() {
    subscriptions.add(topBarContainer.onClickNew().subscribe(aVoid -> {
      navigateToCreateGroup();
    }));

    subscriptions.add(topBarContainer.onClickProfile().subscribe(aVoid -> {
      navigateToProfile();
    }));

    subscriptions.add(topBarContainer.onClickInvite().subscribe(aVoid -> {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.HOME);
      bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
      tagManager.trackEvent(TagManagerUtils.Invites, bundle);
      navigator.openSmsForInvite(this);
    }));

    subscriptions.add(topBarContainer.onOpenCloseSearch().subscribe(open -> {
      if (open) {
        recyclerViewFriends.requestDisallowInterceptTouchEvent(true);
        layoutManager.setScrollEnabled(false);
        searchView.show();
      } else {
        recyclerViewFriends.requestDisallowInterceptTouchEvent(false);
        layoutManager.setScrollEnabled(true);
        searchView.hide();
      }
    }));
  }

  private void initSearch() {
    subscriptions.add(searchView.onShow().subscribe(aVoid -> {
      searchView.setVisibility(View.VISIBLE);
    }));

    subscriptions.add(searchView.onGone().subscribe(aVoid -> {
      searchView.setVisibility(View.GONE);
    }));

    subscriptions.add(searchView.onHangLive().subscribe(recipient -> {
      navigator.navigateToLive(this, recipient, PaletteGrid.get(0));
    }));

    subscriptions.add(searchView.onInvite().subscribe(contact -> {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.SEARCH);
      bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
      tagManager.trackEvent(TagManagerUtils.Invites, bundle);
      shouldOverridePendingTransactions = true;
      navigator.openSmsForInvite(this);
    }));

    subscriptions.add(searchView.onUnblock().subscribe(recipient -> {
      if (recipient instanceof Friendship) {
        Friendship fr = (Friendship) recipient;
        homeGridPresenter.updateFriendship(fr.getId(), fr.isMute(), FriendshipRealm.DEFAULT);
      }
    }));

    searchView.initSearchTextSubscription(topBarContainer.onSearch());
  }

  @Override public void onDeepLink(String url) {
    if (!StringUtils.isEmpty(url)) {
      Uri uri = Uri.parse(url);

      if (uri != null && !StringUtils.isEmpty(uri.getPath())) {
        if (uri.getPath().startsWith("/u/")) {
          searchView.show();
        } else if (uri.getPath().startsWith("/g/")) {
          homeGridPresenter.createMembership(StringUtils.getLastBitFromUrl(url));
        }
      }
    }
  }

  @Override public void renderRecipientList(List<Recipient> recipientList) {
    if (recipientList != null) {
      Bundle bundle = new Bundle();
      bundle.putInt(TagManagerUtils.USER_FRIENDS_COUNT, getCurrentUser().getFriendships().size());
      bundle.putInt(TagManagerUtils.USER_GROUPS_COUNT, getCurrentUser().getMembershipList().size());
      tagManager.setProperty(bundle);
      onRecipientUpdates.onNext(recipientList);
      canEndRefresh = false;
    }
  }

  @Override public void refreshGrid() {

  }

  @Override public void onFriendshipUpdated(Friendship friendship) {

  }

  @Override public void successFacebookLogin() {
    homeGridPresenter.updateUserFacebook(AccessToken.getCurrentAccessToken().getUserId());
    syncContacts();
  }

  @Override public void errorFacebookLogin() {
  }

  @Override public void onMembershipCreated(Membership membership) {

  }

  private void initRegistrationToken() {
    String token = FirebaseInstanceId.getInstance().getToken();
    if (token != null) homeGridPresenter.sendToken(token);
  }

  private void manageDeepLink(Intent intent) {
    if (intent != null) {
      if (intent.getData() != null) {
        homeGridPresenter.getHeadDeepLink(intent.getDataString());
      }
    }
  }

  @Override public void onBackPressed() {
    super.onBackPressed();

    if (!topBarContainer.isSearchMode()) {
      // This is important : Hack to open a dummy activity for 200-500ms (cannot be noticed by user as it is for 500ms
      // and transparent floating activity and auto finishes)
      startActivity(new Intent(this, DummyActivity.class));
      finish();
    } else {
      topBarContainer.closeSearch();
    }
  }

  @Override public UserComponent getComponent() {
    return userComponent;
  }

  @Override public void showLoading() {

  }

  @Override public void hideLoading() {
  }

  @Override public void showError(String message) {
  }

  @Override public Context context() {
    return this;
  }

  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.w("TRIBE", "onConnectionFailed:" + connectionResult);
    Toast.makeText(this, "Google Play Services Error: " + connectionResult.getErrorCode(),
        Toast.LENGTH_SHORT).show();
  }

  private void navigateToProfile() {
    navigator.navigateToProfile(HomeActivity.this);
  }

  private void navigateToCreateGroup() {
    HomeActivity.this.navigator.navigateToCreateGroup(this);
  }

  private void syncContacts() {
    homeGridPresenter.lookupContacts();
    lastSync.set(System.currentTimeMillis());
  }

  /////////////////
  //  BROADCAST  //
  /////////////////

  class NotificationReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {

      NotificationPayload notificationPayload =
          (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

      LiveNotificationView liveNotificationView =
          NotificationUtils.getNotificationViewFromPayload(context, notificationPayload);

      if (liveNotificationView != null) {
        subscriptions.add(liveNotificationView.onClickAction()
            .filter(action -> action.getIntent() != null)
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                action -> navigator.navigateToIntent(HomeActivity.this, action.getIntent())));

        Alerter.create(HomeActivity.this, liveNotificationView).show();
      }
    }
  }
}