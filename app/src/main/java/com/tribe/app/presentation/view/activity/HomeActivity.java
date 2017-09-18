package com.tribe.app.presentation.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.f2prateek.rx.preferences.Preference;
import com.facebook.AccessToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.AppStateMonitor;
import com.jenzz.appstate.RxAppStateMonitor;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.network.WSService;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.scope.HasComponent;
import com.tribe.app.presentation.mvp.presenter.HomePresenter;
import com.tribe.app.presentation.mvp.view.HomeGridMVPView;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.utils.IntentUtils;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.FullscreenNotificationState;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.adapter.HomeListAdapter;
import com.tribe.app.presentation.view.adapter.SectionCallback;
import com.tribe.app.presentation.view.adapter.decorator.DividerDecoration;
import com.tribe.app.presentation.view.adapter.decorator.RecyclerSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.diff.GridDiffCallback;
import com.tribe.app.presentation.view.adapter.helper.HomeListTouchHelperCallback;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.component.TopBarContainer;
import com.tribe.app.presentation.view.component.home.NewChatView;
import com.tribe.app.presentation.view.component.home.SearchView;
import com.tribe.app.presentation.view.component.live.LiveViewFake;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.PopupContainerView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.notifications.ErrorNotificationView;
import com.tribe.app.presentation.view.widget.notifications.NotificationContainerView;
import com.tribe.app.presentation.view.widget.notifications.RatingNotificationView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.view.View.VISIBLE;

public class HomeActivity extends BaseActivity
    implements HasComponent<UserComponent>, ShortcutMVPView, HomeGridMVPView,
    GoogleApiClient.OnConnectionFailedListener, AppStateListener {

  private static final long TWENTY_FOUR_HOURS = 86400000;
  public static final int SETTINGS_RESULT = 101;

  public static Intent getCallingIntent(Context context) {
    return new Intent(context, HomeActivity.class);
  }

  @Inject NotificationManagerCompat notificationManager;

  @Inject HomePresenter homeGridPresenter;

  @Inject HomeListAdapter homeGridAdapter;

  @Inject ScreenUtils screenUtils;

  @Inject PaletteGrid paletteGrid;

  @Inject StateManager stateManager;

  @Inject SoundManager soundManager;

  @Inject MissedCallManager missedCallManager;

  @Inject @AddressBook Preference<Boolean> addressBook;

  @Inject @LastVersionCode Preference<Integer> lastVersion;

  @Inject @LastSync Preference<Long> lastSync;

  @Inject @CallTagsMap Preference<String> callTagsMap;

  @Inject @FullscreenNotificationState Preference<Set<String>> fullScreenNotificationState;

  @BindView(R.id.recyclerViewFriends) RecyclerView recyclerViewFriends;

  @BindView(android.R.id.content) ViewGroup rootView;

  @BindView(R.id.topBarContainer) TopBarContainer topBarContainer;

  @BindView(R.id.searchView) SearchView searchView;

  @BindView(R.id.notificationContainerView) NotificationContainerView notificationContainerView;

  @BindView(R.id.ratingNotificationView) RatingNotificationView ratingNotificationView;

  @BindView(R.id.errorNotificationView) ErrorNotificationView errorNotificationView;

  @BindView(R.id.btnNewChat) NewChatView btnNewChat;

  @BindView(R.id.viewAvatar) AvatarView viewAvatar;

  @BindView(R.id.nativeDialogsView) PopupContainerView popupContainerView;

  @BindView(R.id.txtSyncedContacts) TextViewFont txtSyncedContacts;

  @BindView(R.id.viewFadeInSwipe) View viewFadeInSwipe;

  @BindView(R.id.viewLiveFake) LiveViewFake viewLiveFake;

  // OBSERVABLES
  private UserComponent userComponent;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Scheduler singleThreadExecutor;
  private PublishSubject<List<Recipient>> onRecipientUpdates = PublishSubject.create();
  private PublishSubject<List<Contact>> onNewContacts = PublishSubject.create();
  private PublishSubject<Pair<Integer, Boolean>> onNewContactsInfos = PublishSubject.create();

  // VARIABLES
  private HomeLayoutManager layoutManager;
  private ItemTouchHelper itemTouchHelper;
  private List<Recipient> latestRecipientList;
  private boolean shouldOverridePendingTransactions = false;
  private NotificationReceiver notificationReceiver;
  private boolean receiverRegistered = false;
  private boolean hasSynced = false;
  private boolean canEndRefresh = false;
  private boolean finish = false;
  private AppStateMonitor appStateMonitor;
  private RxPermissions rxPermissions;
  private boolean searchViewDisplayed = false;
  private FirebaseRemoteConfig firebaseRemoteConfig;

  // DIMEN

  @Override protected void onCreate(Bundle savedInstanceState) {
    getWindow().setBackgroundDrawableResource(android.R.color.black);
    super.onCreate(savedInstanceState);

    initDependencyInjector();
    init();
    initUi();
    initDimensions();
    initRegistrationToken();
    initAppState();
    initRecyclerView();
    initTopBar();
    initSearch();
    initPullToRefresh();
    initPreviousCallTags();
    initNewCall();
    initRemoteConfig();
    manageLogin(getIntent());
    manageIntent(getIntent());

    homeGridPresenter.onViewAttached(this);
    homeGridPresenter.reload(hasSynced);
    if (!hasSynced) hasSynced = true;

    subscriptions.add(Observable.
        from(PermissionUtils.PERMISSIONS_CAMERA)
        .map(permission -> rxPermissions.isGranted(permission))
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

    popupAccessFacebookContact();
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    manageIntent(intent);
  }

  @Override protected void onStart() {
    super.onStart();
    tagManager.onStart(this);
    fullScreenNotificationState.set(new HashSet<>());
    if (System.currentTimeMillis() - lastSync.get() > TWENTY_FOUR_HOURS) {
      lookupContacts();
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

    if (finish) return;

    homeGridPresenter.loadContactsOnApp();

    startService(WSService.getCallingIntent(this, null, null));

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

    if (stateManager.shouldDisplay(StateManager.NEW_CALL_POPUP)) {
      stateManager.addTutorialKey(StateManager.NEW_CALL_POPUP);
      popupContainerView.displayPopup(btnNewChat, PopupContainerView.DISPLAY_NEW_CALL_POPUP,
          getResources().getString(R.string.grid_tutorial_new_call));
    } else if (stateManager.shouldDisplay(StateManager.PROFILE_POPUP)) {
      stateManager.addTutorialKey(StateManager.PROFILE_POPUP);
      popupContainerView.displayPopup(viewAvatar, PopupContainerView.DISPLAY_PROFILE_POPUP,
          getString(R.string.grid_tutorial_profile));
    }

    initMissedCall();
    initRecyclerViewCallback();
  }

  @Override protected void onPause() {
    if (receiverRegistered) {
      unregisterReceiver(notificationReceiver);
      receiverRegistered = false;
    }

    super.onPause();
  }

  @Override protected void onDestroy() {
    if (recyclerViewFriends != null) recyclerViewFriends.setAdapter(null);

    if (homeGridPresenter != null) homeGridPresenter.onViewDetached();

    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    if (appStateMonitor != null) {
      appStateMonitor.removeListener(this);
      appStateMonitor.stop();
    }

    if (soundManager != null) soundManager.cancelMediaPlayer();

    stopService();

    super.onDestroy();
  }

  private void displaySyncBanner(String txt) {
    txtSyncedContacts.setText(txt);
    txtSyncedContacts.setVisibility(VISIBLE);
    Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_down_up);
    txtSyncedContacts.startAnimation(anim);
  }

  private void stopService() {
    Intent i = new Intent(this, WSService.class);
    stopService(i);
  }

  private void init() {
    rxPermissions = new RxPermissions(this);
    singleThreadExecutor = Schedulers.from(Executors.newSingleThreadExecutor());
    latestRecipientList = new ArrayList<>();

    subscriptions.add(onNewContacts.observeOn(Schedulers.computation()).map(contactList -> {
      List<Contact> result = new ArrayList<>();
      Map<String, Contact> mapContact = new HashMap<>();

      if (getCurrentUser().getShortcutList() == null) {
        result.addAll(contactList);
      } else {
        for (Contact contact : contactList) {
          boolean shouldAdd = true;

          if (contact.getUserList() != null && contact.getUserList().size() > 0) {
            User linkedUser = contact.getUserList().get(0);

            for (Shortcut shortcut : getCurrentUser().getShortcutList()) {
              if (shortcut.isSingle() && shortcut.isFriend(linkedUser)) shouldAdd = false;
            }

            if (mapContact.containsKey(linkedUser.getId())) {
              shouldAdd = false;
            } else {
              mapContact.put(linkedUser.getId(), contact);
            }
          }

          if (shouldAdd) {
            result.add(contact);
          }
        }
      }

      int nbContacts = result.size();
      boolean hasNewContacts = false;

      for (Contact contact : result) {
        if (contact.isNew()) hasNewContacts = true;
      }

      return new Pair<>(nbContacts, hasNewContacts);
    }).subscribe(onNewContactsInfos));
  }

  private void initMissedCall() {
    if (missedCallManager != null &&
        missedCallManager.getNotificationPayloadList() != null &&
        missedCallManager.getNbrOfMissedCall() > 0) {
      Intent intentUnique = new Intent(BroadcastUtils.BROADCAST_NOTIFICATIONS);
      intentUnique.putExtra(BroadcastUtils.NOTIFICATION_PAYLOAD,
          missedCallManager.buildNotificationBuilderFromMissedCallList());
      sendBroadcast(intentUnique);
    }
  }

  private void initUi() {
    setContentView(R.layout.activity_home);
    ButterKnife.bind(this);
    viewLiveFake.setTranslationX(screenUtils.getWidthPx());
  }

  private void initDimensions() {
  }

  private void initPullToRefresh() {
    subscriptions.add(topBarContainer.onRefresh()
        .doOnNext(aVoid -> canEndRefresh = true)
        .doOnError(throwable -> throwable.printStackTrace())
        .delay(TopBarContainer.MIN_LENGTH, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(bool -> {
          if (canEndRefresh) {
            latestRecipientList.clear();
            homeGridPresenter.reload(false);
            canEndRefresh = false;
          }
        }));
  }

  private void onClickItem(Recipient recipient) {
    if (recipient.getId().equals(Recipient.ID_MORE)) {
      homeGridPresenter.createRoom(TagManagerUtils.INVITE, null, false);
    } else if (recipient.getId().equals(Recipient.ID_VIDEO)) {
      navigator.navigateToVideo(this);
    } else {
      navigator.navigateToLive(this, recipient, PaletteGrid.get(recipient.getPosition()),
          recipient instanceof Invite ? LiveActivity.SOURCE_DRAGGED_AS_GUEST
              : LiveActivity.SOURCE_GRID);
    }
  }

  private void initRecyclerView() {
    initUIRecyclerView();
    subscriptions.add(homeGridAdapter.onChatClick()
        .map(view -> homeGridAdapter.getItemAtPosition(
            recyclerViewFriends.getChildLayoutPosition(view)))
        .subscribe(recipient -> navigator.navigateToChat(this, recipient)));

    subscriptions.add(Observable.merge(homeGridAdapter.onClickMore(), homeGridAdapter.onLongClick())
        .map(view -> homeGridAdapter.getItemAtPosition(
            recyclerViewFriends.getChildLayoutPosition(view)))

        .flatMap(recipient -> DialogFactory.showBottomSheetForRecipient(this, recipient),
            ((recipient, labelType) -> {
              if (labelType != null) {
                if (labelType.getTypeDef().equals(LabelType.HIDE) ||
                    labelType.getTypeDef().equals(LabelType.BLOCK_HIDE)) {
                  Shortcut shortcut = (Shortcut) recipient;
                  homeGridPresenter.updateShortcutStatus(shortcut.getId(),
                      labelType.getTypeDef().equals(LabelType.BLOCK_HIDE) ? ShortcutRealm.BLOCKED
                          : ShortcutRealm.HIDDEN);
                } else if (labelType.getTypeDef().equals(LabelType.MUTE)) {
                  Shortcut shortcut = (Shortcut) recipient;
                  shortcut.setMute(true);
                  homeGridPresenter.muteShortcut(shortcut.getId(), true);
                } else if (labelType.getTypeDef().equals(LabelType.UNMUTE)) {
                  Shortcut shortcut = (Shortcut) recipient;
                  shortcut.setMute(false);
                  homeGridPresenter.muteShortcut(shortcut.getId(), false);
                } else if (labelType.getTypeDef().equals(LabelType.DECLINE)) {
                  Invite invite = (Invite) recipient;
                  homeGridPresenter.declineInvite(invite.getId());
                }
              }

              return recipient;
            }))
        .subscribe());

    subscriptions.add(homeGridAdapter.onClick()
        .map(view -> homeGridAdapter.getItemAtPosition(
            recyclerViewFriends.getChildLayoutPosition(view)))
        .subscribe(recipient -> {
          boolean displayPermissionNotif = notificationContainerView.
              showNotification(null, NotificationContainerView.DISPLAY_PERMISSION_NOTIF);
          if (displayPermissionNotif) {
            notificationContainerView.onAcceptedPermission().subscribe(permissionGranted -> {
              if (permissionGranted) {
                onClickItem(recipient);
              }
            });
          } else {
            onClickItem(recipient);
          }
        }));

    subscriptions.add(onRecipientUpdates.onBackpressureBuffer().map(recipients -> {
      List<Recipient> recipientFinalList = new ArrayList<>();
      for (Recipient recipient : recipients) {
        if (recipient instanceof Invite || !recipient.isLive()) {
          recipientFinalList.add(recipient);
        }
      }

      return recipientFinalList;
    }).subscribeOn(singleThreadExecutor).
        map(recipientList -> {
          DiffUtil.DiffResult diffResult = null;
          List<Recipient> temp = new ArrayList<>();
          temp.add(new Shortcut(Recipient.ID_HEADER));
          temp.addAll(recipientList);

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

  private void initNewCall() {
    subscriptions.add(
        btnNewChat.onNewChat().subscribe(aVoid -> navigateToNewCall(LiveActivity.SOURCE_NEW_CALL)));

    subscriptions.add(
        btnNewChat.onBackToTop().subscribe(aVoid -> recyclerViewFriends.smoothScrollToPosition(0)));
  }

  private void initRemoteConfig() {
    firebaseRemoteConfig = firebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings =
        new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build();
    firebaseRemoteConfig.setConfigSettings(configSettings);

    firebaseRemoteConfig.fetch().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        firebaseRemoteConfig.activateFetched();
      }
    });
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
    subscriptions.add(topBarContainer.onClickProfile().subscribe(aVoid -> navigateToProfile()));

    subscriptions.add(topBarContainer.onClickCallRoulette().subscribe(aVoid -> {
      navigateToNewCall(LiveActivity.SOURCE_CALL_ROULETTE);
    }));

    // subscriptions.add(topBarContainer.onClickInvite().subscribe(aVoid -> {
    //   Bundle bundle = new Bundle();
    //   bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.HOME);
    //   bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
    //   tagManager.trackEvent(TagManagerUtils.Invites, bundle);
    //   homeGridPresenter.createRoom(TagManagerUtils.INVITE, null, false);
    // }));

    subscriptions.add(topBarContainer.onOpenCloseSearch()
        .doOnNext(open -> {
          if (open) {
            recyclerViewFriends.requestDisallowInterceptTouchEvent(true);
            layoutManager.setScrollEnabled(false);
            searchViewDisplayed = true;
            searchView.refactorActions();
            searchView.show();
          } else {
            recyclerViewFriends.requestDisallowInterceptTouchEvent(false);
            layoutManager.setScrollEnabled(true);
            searchViewDisplayed = false;
            searchView.hide();
          }
        })
        .delay(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(open -> {
          if (!open) homeGridPresenter.removeNewStatusContact();
        }));

    topBarContainer.initNewContactsObs((Observable) onNewContactsInfos);
  }

  private void initSearch() {
    subscriptions.add(searchView.onNavigateToSmsForInvites().subscribe(aVoid -> {
      homeGridPresenter.createRoom(TagManagerUtils.INVITE, null, false);
    }));

    subscriptions.add(searchView.onShow().subscribe(aVoid -> searchView.setVisibility(VISIBLE)));

    subscriptions.add(searchView.onGone().subscribe(aVoid -> searchView.setVisibility(View.GONE)));

    subscriptions.add(searchView.onHangLive()
        .subscribe(recipient -> navigator.navigateToLive(this, recipient, PaletteGrid.get(0),
            LiveActivity.SOURCE_SEARCH)));

    subscriptions.add(searchView.onInvite().subscribe(contact -> {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.SEARCH);
      bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
      tagManager.trackEvent(TagManagerUtils.Invites, bundle);
      shouldOverridePendingTransactions = true;
      homeGridPresenter.createRoom(TagManagerUtils.SEARCH, contact.getPhone(), false);
    }));

    subscriptions.add(searchView.onUnblock().subscribe(recipient -> {
      //if (recipient instanceof Friendship) {
      //  Friendship fr = (Friendship) recipient;
      //  homeGridPresenter.updateFriendship(fr.getId(), fr.isMute(), FriendshipRealm.DEFAULT);
      //}
    }));

    searchView.initSearchTextSubscription(topBarContainer.onSearch());

    subscriptions.add(topBarContainer.onSyncContacts().subscribe(aVoid -> syncContacts()));
    subscriptions.add(searchView.onSyncContacts().subscribe(aVoid -> syncContacts()));
  }

  private void initAppState() {
    appStateMonitor = RxAppStateMonitor.create(getApplication());
    appStateMonitor.addListener(this);
    appStateMonitor.start();
  }

  private void initPreviousCallTags() {
    String callTags = callTagsMap.get();
    if (!StringUtils.isEmpty(callTags)) {
      TagManagerUtils.manageTags(tagManager, PreferencesUtils.getMapFromJson(callTagsMap));
      callTagsMap.set("");
    }
  }

  private void declineInvitation(String sessionId) {
    homeGridPresenter.declineInvite(sessionId);
  }

  @Override public void onDeepLink(String url) {
    if (!StringUtils.isEmpty(url)) {
      Uri uri = Uri.parse(url);

      if (uri != null && !StringUtils.isEmpty(uri.getPath())) {
        if (uri.getPath().startsWith("/u/")) {
          searchView.show();
        }
      }
    }
  }

  @Override public void renderRecipientList(List<Recipient> recipientList) {
    if (recipientList != null) {
      //Bundle bundle = new Bundle();
      //bundle.putInt(TagManagerUtils.USER_FRIENDS_COUNT, getCurrentUser().getFriendships().size());
      //tagManager.setProperty(bundle);
      onRecipientUpdates.onNext(recipientList);
      canEndRefresh = false;
    }
  }

  @Override public void refreshGrid() {

  }

  @Override public void successFacebookLogin() {
    homeGridPresenter.updateUserFacebook(getCurrentUser().getId(),
        AccessToken.getCurrentAccessToken().getToken());
    syncContacts();
  }

  @Override public void errorFacebookLogin() {
  }

  private void initRegistrationToken() {
    String token = FirebaseInstanceId.getInstance().getToken();
    if (token != null) homeGridPresenter.sendToken(token);
  }

  private void manageIntent(Intent intent) {
    if (intent != null) {
      if (intent.hasExtra(IntentUtils.FINISH)) {
        finish = intent.getBooleanExtra(IntentUtils.FINISH, false);
        if (finish) {
          finish();
          return;
        }
      } else if (intent.hasExtra(Constants.NOTIFICATION_HOME)) {
        Bundle bundle = new Bundle();
        bundle.putString(TagManagerUtils.CATEGORY,
            intent.getStringExtra(Constants.NOTIFICATION_HOME));
        tagManager.trackEvent(TagManagerUtils.Notification_AppOpen, bundle);

        if (intent.hasExtra(IntentUtils.USER_REGISTERED)) {
          homeGridPresenter.createShortcut(intent.getStringExtra(IntentUtils.USER_REGISTERED));
        }
      } else if (intent.getData() != null) {
        Intent newIntent =
            IntentUtils.getLiveIntentFromURI(this, intent.getData(), LiveActivity.SOURCE_DEEPLINK);
        if (newIntent != null) navigator.navigateToIntent(this, newIntent);
      }
    }
  }

  private void openSmsApp(Intent intent) {
    if (intent != null && intent.hasExtra(Extras.ROOM_LINK_ID)) {
      if (stateManager.shouldDisplay(StateManager.OPEN_SMS)) {
        stateManager.addTutorialKey(StateManager.OPEN_SMS);
        homeGridPresenter.createRoom(TagManagerUtils.ONBOARDING, null, true);
      }
    }
  }

  private void manageLogin(Intent intent) {
    openSmsApp(intent);

    if (intent != null && intent.hasExtra(Extras.IS_FROM_LOGIN)) {
      tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_HomeScreen);
    }
  }

  @Override public void onBackPressed() {
    if (!topBarContainer.isSearchMode() && !searchViewDisplayed) {
      super.onBackPressed();
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

  @Override public void onSyncDone() {
    lastSync.set(System.currentTimeMillis());
    displaySyncBanner(getString(R.string.grid_synced_contacts_banner));
    homeGridPresenter.sendInvitations();
    topBarContainer.onSyncDone();
  }

  @Override public void onSyncStart() {
    displaySyncBanner(getString(R.string.grid_syncing_contacts_banner));
    topBarContainer.onSyncStart();
  }

  @Override public void onSyncError() {
    displaySyncBanner(getString(R.string.grid_sync_failed_contacts_banner));
    topBarContainer.onSyncError();
  }

  @Override public void renderContactsOnApp(List<Contact> contactList) {
    onNewContacts.onNext(contactList);
  }

  @Override
  public void onCreateRoom(Room room, String feature, String phone, boolean shouldOpenSMS) {
    navigator.sendInviteToCall(this, firebaseRemoteConfig, TagManagerUtils.INVITE, room.getLink(),
        phone, shouldOpenSMS);
  }

  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.w("TRIBE", "onConnectionFailed:" + connectionResult);
    Toast.makeText(this, "Google Play Services Error: " + connectionResult.getErrorCode(),
        Toast.LENGTH_SHORT).show();
  }

  private void navigateToProfile() {
    navigator.navigateToProfile(HomeActivity.this);
  }

  private void navigateToNewCall(@LiveActivity.Source String source) {
    HomeActivity.this.navigator.navigateToNewCall(this, source);
  }

  private void syncContacts() {
    rxPermissions.request(PermissionUtils.PERMISSIONS_CONTACTS).subscribe(hasPermission -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerUtils.USER_ADDRESS_BOOK_ENABLED, hasPermission);
      tagManager.setProperty(bundle);

      Bundle bundleBis = new Bundle();
      bundleBis.putBoolean(TagManagerUtils.ACCEPTED, true);
      tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_SystemContacts, bundleBis);
      if (hasPermission) {
        addressBook.set(true);
        homeGridPresenter.lookupContacts();
        searchView.refactorActions();
      } else {
        topBarContainer.onSyncError();
      }
    });
  }

  private void popupAccessFacebookContact() {
    if (stateManager.shouldDisplay(StateManager.FACEBOOK_CONTACT_PERMISSION) &&
        !FacebookUtils.isLoggedIn()) {
      subscriptions.add(DialogFactory.dialog(context(),
          EmojiParser.demojizedText(context().getString(R.string.permission_facebook_popup_title)),
          EmojiParser.demojizedText(
              context().getString(R.string.permission_facebook_popup_message)),
          context().getString(R.string.permission_facebook_popup_ok),
          context().getString(R.string.permission_facebook_popup_ko))
          .filter(x -> x == true)
          .subscribe(a -> {
            homeGridPresenter.loginFacebook();
          }));
      stateManager.addTutorialKey(StateManager.FACEBOOK_CONTACT_PERMISSION);
    }
  }

  private void lookupContacts() {
    if (stateManager.shouldDisplay(StateManager.PERMISSION_CONTACT)) {
      stateManager.addTutorialKey(StateManager.PERMISSION_CONTACT);
      syncContacts();
    }
  }

  @Override public void onAppDidEnterForeground() {
  }

  @Override public void onAppDidEnterBackground() {
    Timber.d("App in background stopping the service");
    stopService();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == Navigator.FROM_LIVE) {
      topBarContainer.displayTooltip();
    }
    if (requestCode == Navigator.FROM_PROFILE) topBarContainer.reloadUserUI();

    if (data != null) {
      if (data.hasExtra(NotificationPayload.CLICK_ACTION_DECLINE)) {
        NotificationPayload notificationPayload = (NotificationPayload) data.getSerializableExtra(
            NotificationPayload.CLICK_ACTION_DECLINE);
        if (notificationPayload != null) {
          displayDeclinedCallNotification(notificationPayload);
        }
      } else if (data.getBooleanExtra(ErrorNotificationView.DISPLAY_ERROR_NOTIF, false)) {
        errorNotificationView.displayView();
      } else if (!notificationContainerView.showNotification(data, null)) {
        displayRatingNotifView(data);
      }
    }
  }

  private void displayRatingNotifView(Intent data) {
    if (data.getBooleanExtra(RatingNotificationView.DISPLAY_RATING_NOTIF, false)) {
      long timeout = data.getLongExtra(LiveActivity.TIMEOUT_RATING_NOTIFICATON, 0);
      String roomId = data.getStringExtra(LiveActivity.ROOM_ID);
      ratingNotificationView.displayView(timeout, roomId);
    }
  }

  /////////////////
  //  BROADCAST  //
  /////////////////
  private void initUIRecyclerView() {
    layoutManager = new HomeLayoutManager(context());
    layoutManager.setAutoMeasureEnabled(false);
    recyclerViewFriends.setHasFixedSize(true);
    recyclerViewFriends.setLayoutManager(layoutManager);
    recyclerViewFriends.setItemAnimator(null);
    recyclerViewFriends.addItemDecoration(
        new DividerDecoration(context(), ContextCompat.getColor(context(), R.color.grey_divider),
            screenUtils.dpToPx(0.5f), getSectionCallback(homeGridAdapter.getItems())));
    homeGridAdapter.setItems(new ArrayList<>());
    recyclerViewFriends.setAdapter(homeGridAdapter);

    // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(0, 50);
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(1, 50);
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(2, 50);
    recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(3, 50);
    recyclerViewFriends.setItemViewCacheSize(30);
    recyclerViewFriends.setDrawingCacheEnabled(true);
    recyclerViewFriends.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

    RecyclerSectionItemDecoration sectionItemDecoration = new RecyclerSectionItemDecoration(
        getResources().getDimensionPixelSize(R.dimen.list_home_header_height), true,
        getSectionCallback(homeGridAdapter.getItems()), screenUtils);
    recyclerViewFriends.addItemDecoration(sectionItemDecoration);
  }

  private void initRecyclerViewCallback() {
    viewFadeInSwipe.setVisibility(View.GONE);
    viewFadeInSwipe.setAlpha(0);
    viewLiveFake.setTranslationX(screenUtils.getWidthPx());

    HomeListTouchHelperCallback callback = new HomeListTouchHelperCallback(homeGridAdapter);

    if (itemTouchHelper == null) {
      itemTouchHelper = new ItemTouchHelper(callback);
    }

    itemTouchHelper.attachToRecyclerView(null);
    itemTouchHelper.attachToRecyclerView(recyclerViewFriends);

    subscriptions.add(callback.onDxChange().subscribe(pairPosDx -> {
      if (pairPosDx.second == 0) {
        viewFadeInSwipe.setVisibility(View.GONE);
        viewFadeInSwipe.setAlpha(0);
      } else {
        Recipient recipient = homeGridAdapter.getItemAtPosition(pairPosDx.first);
        viewLiveFake.setLive(LiveActivity.getLive(recipient, PaletteGrid.get(pairPosDx.first),
            recipient instanceof Invite ? LiveActivity.SOURCE_DRAGGED_AS_GUEST
                : LiveActivity.SOURCE_GRID), recipient);
        viewFadeInSwipe.setVisibility(View.VISIBLE);
        viewFadeInSwipe.setTranslationX(pairPosDx.second);
        viewFadeInSwipe.setAlpha(Math.abs(pairPosDx.second) / (float) screenUtils.getWidthPx());
        viewLiveFake.setTranslationX(screenUtils.getWidthPx() + pairPosDx.second);
      }
    }));

    subscriptions.add(callback.onSwipedItem().subscribe(position -> {
      Recipient recipient = homeGridAdapter.getItemAtPosition(position);
      navigator.navigateToLiveFromSwipe(this, homeGridAdapter.getItemAtPosition(position),
          PaletteGrid.get(position),
          recipient instanceof Invite ? LiveActivity.SOURCE_DRAGGED_AS_GUEST
              : LiveActivity.SOURCE_GRID);
    }));
  }

  private SectionCallback getSectionCallback(final List<Recipient> recipientList) {
    return new SectionCallback() {
      @Override public boolean isSection(int position) {
        return position == 1 ||
            (position > 0 &&
                recipientList.get(position).getSectionType() !=
                    recipientList.get(position - 1).getSectionType());
      }

      @Override public int getSectionType(int position) {
        return recipientList.get(position).getSectionType();
      }
    };
  }

  private void displayDeclinedCallNotification(NotificationPayload notificationPayload) {
    LiveNotificationView liveNotificationView =
        NotificationUtils.getNotificationViewFromPayload(this, notificationPayload,
            missedCallManager);
    Alerter.create(HomeActivity.this, liveNotificationView).show();
  }

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {

  }

  @Override public void onShortcutCreatedError() {

  }

  @Override public void onShortcutRemovedSuccess() {

  }

  @Override public void onShortcutRemovedError() {

  }

  @Override public void onShortcutUpdatedSuccess(Shortcut shortcut) {

  }

  @Override public void onShortcutUpdatedError() {

  }

  @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {

  }

  class NotificationReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      NotificationPayload notificationPayload =
          (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

      LiveNotificationView liveNotificationView =
          NotificationUtils.getNotificationViewFromPayload(context, notificationPayload,
              missedCallManager);

      if (liveNotificationView != null) {
        subscriptions.add(liveNotificationView.onClickAction()
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(action -> {
              if (action.getId().equals(NotificationUtils.ACTION_DECLINE)) {
                declineInvitation(action.getSessionId());
              } else if (action.getId().equals(NotificationUtils.ACTION_ADD_FRIEND)) {
                homeGridPresenter.createShortcut(action.getUserId());
              } else if (action.getIntent() != null) {
                navigator.navigateToIntent(HomeActivity.this, action.getIntent());
              }
            }));

        Alerter.create(HomeActivity.this, liveNotificationView).show();
      }
    }
  }
}