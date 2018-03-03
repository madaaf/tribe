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
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.f2prateek.rx.preferences.Preference;
import com.facebook.AccessToken;
import com.facebook.accountkit.AccountKitLoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.ContactAB;
import com.tribe.app.domain.entity.ContactFB;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.TribeBroadcastReceiver;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.scope.HasComponent;
import com.tribe.app.presentation.mvp.presenter.HomePresenter;
import com.tribe.app.presentation.mvp.view.HomeGridMVPView;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.utils.IntentUtils;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.Triplet;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.FullscreenNotificationState;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.view.NotifView;
import com.tribe.app.presentation.view.NotificationModel;
import com.tribe.app.presentation.view.adapter.HomeListAdapter;
import com.tribe.app.presentation.view.adapter.SectionCallback;
import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.decorator.HomeListDividerDecoration;
import com.tribe.app.presentation.view.adapter.decorator.HomeSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.delegate.contact.UserToAddAdapterDelegate;
import com.tribe.app.presentation.view.adapter.diff.GridDiffCallback;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.component.home.SearchView;
import com.tribe.app.presentation.view.component.home.TopBarContainer;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ListUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.PopupContainerView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.chat.ChatActivity;
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
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.view.View.VISIBLE;
import static com.tribe.app.presentation.view.ShortcutUtil.createShortcutSupport;
import static com.tribe.app.presentation.view.activity.AuthActivity.APP_REQUEST_CODE;

public class HomeActivity extends BaseActivity
    implements HasComponent<UserComponent>, ShortcutMVPView, HomeGridMVPView,
    GoogleApiClient.OnConnectionFailedListener {

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

  @Inject DateUtils dateUtils;

  @Inject User user;

  @Inject @AddressBook Preference<Boolean> addressBook;

  @Inject @LastVersionCode Preference<Integer> lastVersion;

  @Inject @LastSync Preference<Long> lastSync;

  @Inject @FullscreenNotificationState Preference<Set<String>> fullScreenNotificationState;

  @Inject RxImagePicker rxImagePicker;

  @Inject RxFacebook rxFacebook;

  @BindView(R.id.recyclerViewFriends) RecyclerView recyclerViewFriends;

  @BindView(android.R.id.content) ViewGroup rootView;

  @BindView(R.id.topBarContainer) TopBarContainer topBarContainer;

  @BindView(R.id.searchView) SearchView searchView;

  @BindView(R.id.notificationContainerView) NotificationContainerView notificationContainerView;

  @BindView(R.id.ratingNotificationView) RatingNotificationView ratingNotificationView;

  @BindView(R.id.errorNotificationView) ErrorNotificationView errorNotificationView;

  @BindView(R.id.viewAvatar) AvatarView viewAvatar;

  @BindView(R.id.nativeDialogsView) PopupContainerView popupContainerView;

  @BindView(R.id.txtSyncedContacts) TextViewFont txtSyncedContacts;

  @BindView(R.id.test) FrameLayout test;

  // OBSERVABLES
  private UserComponent userComponent;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Scheduler singleThreadExecutor;
  private PublishSubject<List<Recipient>> onRecipientUpdates = PublishSubject.create();
  private PublishSubject<Shortcut> onSupportUpdate = PublishSubject.create();
  private PublishSubject<List<Contact>> onNewContactsOnApp = PublishSubject.create();
  private PublishSubject<List<Contact>> onNewContactsInvite = PublishSubject.create();
  private PublishSubject<List<Contact>> onNewContactsFBInvite = PublishSubject.create();
  private PublishSubject<Pair<Integer, Boolean>> onNewContactsInfos = PublishSubject.create();

  // VARIABLES
  private HomeLayoutManager layoutManager;
  private List<HomeAdapterInterface> latestRecipientList;
  private TribeBroadcastReceiver notificationReceiver;
  private NotificationReceiverSupport notificationReceiverSupport;
  private boolean shouldOverridePendingTransactions = false, receiverRegistered = false, hasSynced =
      false, canEndRefresh = false, finish = false, searchViewDisplayed = false,
      shouldNavigateToChat = false;
  private RxPermissions rxPermissions;
  private FirebaseRemoteConfig firebaseRemoteConfig;
  private Shortcut supportShortcut = createShortcutSupport();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initDependencyInjector();
    init();
    initUi();
    initDimensions();
    initRegistrationToken();
    initRecyclerView();
    initTopBar();
    initSearch();
    initPullToRefresh();
    initRemoteConfig();
    manageLogin(getIntent());
    manageIntent(getIntent());

    RxJavaHooks.enableAssemblyTracking();

    homeGridPresenter.onViewAttached(this);
    homeGridPresenter.reload(hasSynced);
    if (!hasSynced) {
      hasSynced = true;
    }

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

    RxJavaHooks.enableAssemblyTracking();
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

    // https://stackoverflow.com/questions/36634008/why-notificationmanagercompatcancelall-gets-securityexception
    try {
      notificationManager.cancelAll();
    } catch (SecurityException ex) {
      Timber.e("NotificationManager.cancelAll()", ex);
    }
  }

  @Override protected void onStop() {
    tagManager.onStop(this);

    super.onStop();
  }

  @Override protected void onResume() {
    super.onResume();
    if (finish) return;

    if (shouldOverridePendingTransactions) {
      overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
      shouldOverridePendingTransactions = false;
    }

    if (!receiverRegistered) {
      if (notificationReceiver == null) notificationReceiver = new TribeBroadcastReceiver(this);
      if (notificationReceiverSupport == null) {
        notificationReceiverSupport = new NotificationReceiverSupport();
      }

      registerReceiver(notificationReceiver,
          new IntentFilter(BroadcastUtils.BROADCAST_NOTIFICATIONS));

      registerReceiver(notificationReceiverSupport,
          new IntentFilter(BroadcastUtils.BROADCAST_NOTIFICATIONS));

      subscriptions.add(notificationReceiver.onDeclineInvitation()
          .subscribe(roomId -> homeGridPresenter.declineInvite(roomId)));

      receiverRegistered = true;
    }

    homeGridPresenter.reload(hasSynced);
  }

  @Override protected void onPause() {
    if (receiverRegistered) {
      unregisterReceiver(notificationReceiver);
      unregisterReceiver(notificationReceiverSupport);
      receiverRegistered = false;
    }

    super.onPause();
  }

  @Override protected void onDestroy() {
    if (recyclerViewFriends != null) recyclerViewFriends.setAdapter(null);

    if (homeGridPresenter != null) homeGridPresenter.onViewDetached();

    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();

    if (soundManager != null) soundManager.cancelMediaPlayer();

    super.onDestroy();
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_left);
  }

  private void displaySyncBanner(String txt) {
    txtSyncedContacts.setText(txt);
    txtSyncedContacts.setVisibility(VISIBLE);
    Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_down_up);
    txtSyncedContacts.startAnimation(anim);
  }

  private void init() {
    rxPermissions = new RxPermissions(this);
    singleThreadExecutor = Schedulers.from(Executors.newSingleThreadExecutor());
    latestRecipientList = new ArrayList<>();

    subscriptions.add(onNewContactsOnApp.observeOn(Schedulers.computation()).map(contactList -> {
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

  private void initUi() {
    setContentView(R.layout.activity_home);
    ButterKnife.bind(this);
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
            topBarContainer.endRefresh();
            latestRecipientList.clear();
            homeGridPresenter.reload(false);
            canEndRefresh = false;
          }
        }));
  }

  private void onClickItem(Recipient recipient) {
    navigator.navigateToLive(this, recipient,
        recipient instanceof Invite ? LiveActivity.SOURCE_DRAGGED_AS_GUEST
            : LiveActivity.SOURCE_GRID, recipient.getSectionTag(), null);
  }

  private void initRecyclerView() {
    initUIRecyclerView();
    subscriptions.add(Observable.merge(homeGridAdapter.onChatClick()
            .map(view -> (Recipient) homeGridAdapter.getItemAtPosition(
                recyclerViewFriends.getChildLayoutPosition(view))), homeGridAdapter.onMainClick()
            .map(view -> (Recipient) homeGridAdapter.getItemAtPosition(
                recyclerViewFriends.getChildLayoutPosition(view))), searchView.onClickChat(),
        searchView.onMainClick()).subscribe(item -> navigateToChat(item)));

    subscriptions.add(Observable.merge(homeGridAdapter.onLiveClick()
        .map(view -> (Recipient) homeGridAdapter.getItemAtPosition(
            recyclerViewFriends.getChildLayoutPosition(view))), searchView.onClickLive())
        .debounce(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(item -> onClickItem(item)));

    subscriptions.add(homeGridAdapter.onAddUser().map(view -> {
      HomeAdapterInterface user =
          homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view));
      int position = recyclerViewFriends.getChildAdapterPosition(view);
      return new Pair(position, user);
    }).doOnError(throwable -> throwable.printStackTrace()).subscribe(pair -> {
      UserToAddAdapterDelegate.UserToAddViewHolder vh =
          (UserToAddAdapterDelegate.UserToAddViewHolder) recyclerViewFriends.findViewHolderForAdapterPosition(
              (Integer) pair.first);
      User user = (User) pair.second;
      homeGridPresenter.createShortcutFromSuggestedFriend(vh, user.getId());
    }));

    subscriptions.add(Observable.merge(homeGridAdapter.onClickMore(), homeGridAdapter.onLongClick())
        .map(view -> homeGridAdapter.getItemAtPosition(
            recyclerViewFriends.getChildLayoutPosition(view)))
        .flatMap(item -> DialogFactory.showBottomSheetForRecipient(this, (Recipient) item),
            ((recipient, labelType) -> {
              Shortcut shortcut = null;

              if (recipient instanceof Shortcut) {
                shortcut = (Shortcut) recipient;
              } else if (recipient instanceof Invite) {
                Invite invite = (Invite) recipient;
                shortcut = invite.getShortcut();
              }

              if (labelType != null) {
                if (shortcut != null) {
                  if (labelType.getTypeDef().equals(LabelType.HIDE)) {
                    homeGridPresenter.updateShortcutStatus(shortcut.getId(), ShortcutRealm.HIDDEN);
                  } else if (labelType.getTypeDef().equals(LabelType.MUTE)) {
                    shortcut.setMute(true);
                    homeGridPresenter.muteShortcut(shortcut.getId(), true);
                  } else if (labelType.getTypeDef().equals(LabelType.UNMUTE)) {
                    shortcut.setMute(false);
                    homeGridPresenter.muteShortcut(shortcut.getId(), false);
                  } else if (labelType.getTypeDef().equals(LabelType.SCORES)) {
                    navigateToLeaderboardsShortcut(shortcut);
                  }
                }

                if (labelType.getTypeDef().equals(LabelType.DECLINE)) {
                  Invite invite = (Invite) recipient;
                  homeGridPresenter.declineInvite(invite.getId());
                }
              }

              return new Triplet<LabelType, Shortcut, HomeAdapterInterface>(labelType, shortcut,
                  recipient);
            }))
        .filter(
            pair -> pair.first.getTypeDef().equals(LabelType.CUSTOMIZE) || pair.first.getTypeDef()
                .equals(LabelType.BLOCK_HIDE))
        .flatMap(pair -> {
          if (pair.first.getTypeDef().equals(LabelType.CUSTOMIZE)) {
            return DialogFactory.showBottomSheetForCustomizeShortcut(this);
          } else {
            if (pair.second != null && pair.second.isSingle()) {
              subscriptions.add(DialogFactory.dialog(this,
                  getString(R.string.home_block_shortcut_title, pair.second.getDisplayName()),
                  getString(R.string.home_block_shortcut_message),
                  getString(R.string.home_block_shortcut_validate),
                  getString(R.string.action_cancel))
                  .filter(aBoolean -> aBoolean)
                  .subscribe(aBoolean -> {
                    homeGridPresenter.updateShortcutStatus(pair.second.getId(),
                        ShortcutRealm.BLOCKED);
                    if (pair.third instanceof Invite) {
                      Invite invite = (Invite) pair.third;
                      homeGridPresenter.declineInvite(invite.getId());
                    }
                  }));
            } else if (pair.second != null) {
              subscriptions.add(
                  DialogFactory.dialog(this, getString(R.string.home_block_group_shortcut_title),
                      getString(R.string.home_block_group_shortcut_message),
                      getString(R.string.home_block_group_shortcut_validate),
                      getString(R.string.action_cancel))
                      .filter(aBoolean -> aBoolean)
                      .subscribe(aBoolean -> {
                        homeGridPresenter.updateShortcutStatus(pair.second.getId(),
                            ShortcutRealm.BLOCKED);
                        if (pair.third instanceof Invite) {
                          Invite invite = (Invite) pair.third;
                          homeGridPresenter.declineInvite(invite.getId());
                        }
                      }));
            }

            return Observable.empty();
          }
        }, (pair, labelType) -> {
          Shortcut shortcut = pair.second;

          if (labelType != null && shortcut != null) {
            if (labelType.getTypeDef().equals(LabelType.CHANGE_NAME)) {
              subscriptions.add(
                  DialogFactory.inputDialog(this, getString(R.string.shortcut_update_name_title),
                      getString(R.string.shortcut_update_name_description),
                      getString(R.string.shortcut_update_name_validate),
                      getString(R.string.action_cancel), InputType.TYPE_CLASS_TEXT)
                      .subscribe(s -> homeGridPresenter.updateShortcutName(shortcut.getId(), s)));
            }
          }

          return Pair.create(labelType, shortcut);
        })
        .filter(pair -> pair.first.getTypeDef().equals(LabelType.CHANGE_PICTURE))
        .flatMap(pair -> DialogFactory.showBottomSheetForCamera(this), (pair, labelType) -> {
          if (labelType.getTypeDef().equals(LabelType.OPEN_CAMERA)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA)
                .subscribe(uri -> homeGridPresenter.updateShortcutPicture(pair.second.getId(),
                    uri.toString())));
          } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY)
                .subscribe(uri -> homeGridPresenter.updateShortcutPicture(pair.second.getId(),
                    uri.toString())));
          }

          return null;
        })
        .subscribe());

    subscriptions.add(homeGridAdapter.onClick() // TODO MADA
        .map(view -> homeGridAdapter.getItemAtPosition(
            recyclerViewFriends.getChildLayoutPosition(view))).subscribe(item -> {
          Recipient recipient = (Recipient) item;
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

    subscriptions.add(homeGridAdapter.onClickFb().subscribe(aVoid -> {
      homeGridPresenter.loginFacebook();
      Timber.e("onClickFb");
      popupAccessFacebookContact();
    }));

    subscriptions.add(homeGridAdapter.onClickAddressBook().subscribe(aVoid -> {
      Timber.e("onClickAddressBook");
      syncContacts();
    }));

    subscriptions.add(homeGridAdapter.onInvite()
        .map(view -> homeGridAdapter.getItemAtPosition(
            recyclerViewFriends.getChildLayoutPosition(view)))
        .doOnError(throwable -> throwable.printStackTrace())
        .subscribe(o -> {
          if (o instanceof ContactAB) {
            ContactAB contact = (ContactAB) o;
            invite(contact);
          } else if (o instanceof ContactFB) {
            ArrayList<String> contactFBList = new ArrayList<>();
            ContactFB contactFB = (ContactFB) o;
            contactFBList.add(contactFB.getId());

            subscriptions.add(DialogFactory.dialog(this, contactFB.getDisplayName(),
                getString(R.string.facebook_invite_popup_message),
                EmojiParser.demojizedText(getString(R.string.facebook_invite_popup_validate)),
                getString(R.string.action_cancel)).filter(x -> x).subscribe(a -> {
              rxFacebook.notifyFriends(context(), contactFBList);
              displayFacebookNotification();
            }));
          }
        }));

    subscriptions.add(Observable.combineLatest(onRecipientUpdates.onBackpressureLatest(),
        onNewContactsOnApp.onBackpressureLatest(), onNewContactsInvite.onBackpressureLatest(),
        onNewContactsFBInvite.onBackpressureLatest(), onSupportUpdate.onBackpressureLatest(),
        (recipientList, contactsOnApp, contactsInvite, contactsFBInvite, support) -> {
          List<HomeAdapterInterface> finalList = new ArrayList<>();

          if (!support.isRead()) {
            finalList.add(support);
          } else {
            recipientList.add(support);
          }

          Set<String> addedUsers = new HashSet<>();

          int realFriendsCount = 0;

          for (Recipient recipient : recipientList) {
            if (recipient instanceof Invite || !recipient.isLive()) {
              finalList.add(recipient);
            }

            if (recipient instanceof Shortcut) {
              Shortcut shortcut = (Shortcut) recipient;
              if (shortcut.isSingle()) {
                realFriendsCount++;
                addedUsers.add(shortcut.getSingleFriend().getId());
              }
            }
          }

          Bundle bundle = new Bundle();
          bundle.putInt(TagManagerUtils.USER_FRIENDS_COUNT, realFriendsCount);
          tagManager.setProperty(bundle);

          for (Contact contact : contactsOnApp) {
            if (contact.getUserList() != null && contact.getUserList().size() > 0) {
              User user = contact.getUserList().get(0);
              if (!addedUsers.contains(user.getId())) finalList.add(user);
            }
          }

          if (!FacebookUtils.isLoggedIn()) {
            finalList.add(new Contact(Contact.FACEBOOK_ID));
          }

          if (!PermissionUtils.hasPermissionsContact(rxPermissions)) {
            finalList.add(new Contact(Contact.ADDRESS_BOOK_ID));
          }

          finalList.addAll(contactsInvite);
          finalList.addAll(contactsFBInvite);
          List<HomeAdapterInterface> refactordList = new ArrayList<>();

          for (HomeAdapterInterface u : finalList) {
            if (!refactordList.contains(u)) {
              refactordList.add(u);
            }
          }

          return refactordList;
        }).subscribeOn(singleThreadExecutor).
        map(recipientList -> {
          DiffUtil.DiffResult diffResult = null;
          List<HomeAdapterInterface> temp = new ArrayList<>();
          temp.addAll(recipientList);
          ListUtils.addEmptyItemsHome(temp);
          if (latestRecipientList.size() != 0) {
            diffResult = DiffUtil.calculateDiff(new GridDiffCallback(latestRecipientList, temp));
            homeGridAdapter.setItems(temp);
          }

          latestRecipientList.clear();
          latestRecipientList.addAll(temp); // TODO #2
          return diffResult;
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(diffResult -> {
      if (diffResult != null) {
        diffResult.dispatchUpdatesTo(homeGridAdapter);
        layoutManager.scrollToPositionWithOffset(0, 0);
      } else {
        homeGridAdapter.setItems(latestRecipientList);
        homeGridAdapter.notifyDataSetChanged();
        if (latestRecipientList.size() != 0) layoutManager.scrollToPositionWithOffset(0, 0);
      }
    }));
  }

  private void displayFacebookNotification() {
    List<NotificationModel> list = new ArrayList<>();
    NotifView view = new NotifView(getBaseContext());
    NotificationModel a = NotificationUtils.getFbNotificationModel(this);
    list.add(a);
    view.show(this, list);
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
    subscriptions.add(
        topBarContainer.onClickProfile().subscribe(aVoid -> navigator.navigateToProfile(this)));

    subscriptions.add(topBarContainer.onBack().subscribe(aVoid -> finish()));

    subscriptions.add(topBarContainer.onOpenCloseSearch()
        .doOnNext(open -> {
          if (open) {
            openSearch();
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

  private void openSearch() {
    recyclerViewFriends.requestDisallowInterceptTouchEvent(true);
    layoutManager.setScrollEnabled(false);
    searchViewDisplayed = true;
    searchView.refactorActions();
    searchView.show();
  }

  private void initSearch() {
    subscriptions.add(searchView.onNavigateToSmsForInvites().subscribe(aVoid -> {
      homeGridPresenter.createRoom(TagManagerUtils.INVITE, null, false);
    }));

    subscriptions.add(searchView.onShow().subscribe(aVoid -> searchView.setVisibility(VISIBLE)));

    subscriptions.add(searchView.onGone().subscribe(aVoid -> searchView.setVisibility(View.GONE)));

    subscriptions.add(searchView.onHangLive()
        .subscribe(
            recipient -> navigator.navigateToLive(this, recipient, LiveActivity.SOURCE_SEARCH,
                recipient.getSectionTag(), null)));

    subscriptions.add(searchView.onInvite().subscribe(contact -> invite(contact)));

    subscriptions.add(searchView.onUnblock().subscribe(recipient -> {
      if (recipient instanceof Shortcut) {
        Shortcut shortcut = (Shortcut) recipient;
        homeGridPresenter.updateShortcutStatus(shortcut.getId(), ShortcutRealm.DEFAULT);
      }
    }));

    searchView.initSearchTextSubscription(topBarContainer.onSearch());

    subscriptions.add(topBarContainer.onSyncContacts().subscribe(aVoid -> syncContacts()));
    subscriptions.add(searchView.onSyncContacts().subscribe(aVoid -> syncContacts()));
  }

  private void invite(ContactAB contact) {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.SEARCH);
    bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
    tagManager.trackEvent(TagManagerUtils.Invites, bundle);
    shouldOverridePendingTransactions = true;
    homeGridPresenter.createRoom(TagManagerUtils.SEARCH, contact.getPhone(), false);
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
      onSupportUpdate.onNext(supportShortcut);
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
      homeGridPresenter.createRoom(TagManagerUtils.ONBOARDING, null, true);
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

  boolean isBannedUser = false;

  @Override public void onBannedUser(User user) {
    if (isBannedUser) {
      return;
    }
    if (user.isRandom_banned_permanently()) {
      subscriptions.add(
          DialogFactory.dialog(this, getString(R.string.error_just_banned_permanently_title),
              getString(R.string.error_just_banned_permanently_message),
              getString(R.string.walkthrough_action_step2), null)
              .filter(aBoolean -> aBoolean)
              .subscribe());
      isBannedUser = true;
    } else if (user.getRandom_banned_until() != null && !dateUtils.isBefore(
        user.getRandom_banned_until(), dateUtils.getUTCTimeAsDate())) {

      subscriptions.add(
          DialogFactory.dialog(this, getString(R.string.error_just_banned_temporary_title),
              getString(R.string.error_just_banned_temporary_message),
              getString(R.string.walkthrough_action_step2), null)
              .filter(aBoolean -> aBoolean)
              .subscribe());
      isBannedUser = true;
    }
  }

  @Override public void renderContactsOnApp(List<Contact> contactList) {
    onNewContactsOnApp.onNext(contactList);
  }

  @Override public void renderContactsInvite(List<Contact> contactList) {
    onNewContactsInvite.onNext(contactList);
  }

  @Override public void renderContactsFBInvite(List<Contact> contactList) {
    onNewContactsFBInvite.onNext(contactList);
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

  private void navigateToLeaderboardsShortcut(Shortcut shortcut) {
    User friend = shortcut.getSingleFriend();
    navigator.navigateToLeaderboards(HomeActivity.this, friend);
  }

  private void navigateToNewCall(@LiveActivity.Source String source, String gameId) {
    HomeActivity.this.navigator.navigateToNewCall(this, source, gameId);
  }

  private void navigateToNewGame() {
    HomeActivity.this.navigator.navigateToGameStoreNewGame(this);
  }

  private void navigateToChat(Recipient recipient) {
    if (recipient != null && recipient.isSupport()) {
      supportShortcut.setRead(true);
    }

    if (!recipient.isRead()) {
      String shortcutId = "";

      if (recipient instanceof Invite) {
        Invite invite = (Invite) recipient;
        if (invite.getShortcut() != null) shortcutId = invite.getShortcut().getId();
      } else {
        shortcutId = recipient.getId();
      }

      if (!StringUtils.isEmpty(shortcutId)) homeGridPresenter.readShortcut(shortcutId);
    }

    if (recipient instanceof Shortcut) {
      navigator.navigateToChat(this, recipient, null, recipient.getSectionTag(), false);
    } else {
      if (((Invite) recipient).getShortcut() != null) {
        navigator.navigateToChat(this, recipient, null, recipient.getSectionTag(), false);
      } else {
        List<String> list = ((Invite) recipient).getRoomUserIds();
        String[] array = new String[list.size()];
        shouldNavigateToChat = true;
        homeGridPresenter.createShortcut(list.toArray(array));
      }
    }
  }

  @Override public void onShortcutCreatedFromSuggestedFriendSuccess(Shortcut shortcut,
      UserToAddAdapterDelegate.UserToAddViewHolder vh) {
    if (vh != null) {
      vh.progressView.setVisibility(View.INVISIBLE);
      vh.btnAdd.setImageResource(R.drawable.picto_added);
      vh.btnAdd.setVisibility(View.VISIBLE);
      // TODO TIAGO
      // block UI 1s before to delete the line ( vs : TODO #2 )
    }
  }

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {
    if (shouldNavigateToChat) {
      navigator.navigateToChat(this, shortcut, null, shortcut.getSectionTag(), false);
      shouldNavigateToChat = false;
    }
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
    if (stateManager.shouldDisplay(StateManager.FACEBOOK_CONTACT_PERMISSION)
        && !FacebookUtils.isLoggedIn()) {
      subscriptions.add(DialogFactory.dialog(context(),
          EmojiParser.demojizedText(context().getString(R.string.permission_facebook_popup_title)),
          EmojiParser.demojizedText(
              context().getString(R.string.permission_facebook_popup_message)),
          context().getString(R.string.permission_facebook_popup_ok),
          context().getString(R.string.permission_facebook_popup_ko))
          .filter(x -> x == true)
          .subscribe(a -> homeGridPresenter.loginFacebook()));
      stateManager.addTutorialKey(StateManager.FACEBOOK_CONTACT_PERMISSION);
    }
  }

  private void lookupContacts() {
    syncContacts();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
      AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
      if (loginResult.getError() != null) {
        Timber.e("login error " + loginResult.getError());
      } else {
        if (loginResult.getAccessToken() != null) {
          searchView.onSuccessChangeNumber();
        }
      }
    }

    if (requestCode == Navigator.FROM_PROFILE) {
      topBarContainer.reloadUserUI();
    } else if (requestCode == Navigator.FROM_CHAT && data != null && data.hasExtra(
        ChatActivity.EXTRA_SHORTCUT_ID)) {
      homeGridPresenter.updateShortcutLeaveOnlineUntil(
          data.getStringExtra(ChatActivity.EXTRA_SHORTCUT_ID));
    } else if (requestCode == Navigator.FROM_NEW_GAME && data != null) {
      //String gameId = data.getStringExtra(GameStoreActivity.GAME_ID);
      //boolean callRoulette = data.getBooleanExtra(GameStoreActivity.CALL_ROULETTE, false);
      //Shortcut shortcut = (Shortcut) data.getSerializableExtra(GameStoreActivity.SHORTCUT);
      //subscriptions.add(Observable.timer(250, TimeUnit.MILLISECONDS)
      //    .observeOn(AndroidSchedulers.mainThread())
      //    .subscribe(aLong -> {
      //      if (callRoulette) {
      //        navigateToNewCall(LiveActivity.SOURCE_CALL_ROULETTE, gameId);
      //      } else if (shortcut != null) {
      //        if (!StringUtils.isEmpty(gameId)) {
      //          navigator.navigateToLive(this, shortcut, LiveActivity.SOURCE_SHORTCUT_ITEM,
      //              TagManagerUtils.SECTION_SHORTCUT, gameId);
      //        } else {
      //          navigateToChat(shortcut);
      //        }
      //      }
      //    }));
    } else if (requestCode == Navigator.FROM_LIVE && data != null && data.hasExtra(
        LiveActivity.USER_IDS_FOR_NEW_SHORTCUT)) {
      HashSet<String> userIds =
          (HashSet<String>) data.getSerializableExtra(LiveActivity.USER_IDS_FOR_NEW_SHORTCUT);
      homeGridPresenter.createShortcut(userIds.toArray(new String[userIds.size()]));
    } else if (requestCode == Navigator.FROM_LIVE && data != null && data.hasExtra(
        LiveActivity.LAUNCH_SEARCH)) {
      topBarContainer.openSearch();
    } else if (requestCode == Navigator.FROM_LIVE && data != null && data.hasExtra(
        LiveActivity.LAUNCH_DICE)) {
      navigateToNewCall(LiveActivity.SOURCE_CALL_ROULETTE, null);
    } else if (data != null) {
      if (data.hasExtra(NotificationPayload.CLICK_ACTION_DECLINE)) {
        NotificationPayload notificationPayload = (NotificationPayload) data.getSerializableExtra(
            NotificationPayload.CLICK_ACTION_DECLINE);
        if (notificationPayload != null) {
          displayDeclinedCallNotification(notificationPayload);
        }
      } else if (data.getBooleanExtra(ErrorNotificationView.DISPLAY_ERROR_NOTIF, false)) {
        errorNotificationView.displayView();
      }
    }
  }

  private void initUIRecyclerView() {
    layoutManager = new HomeLayoutManager(context());
    layoutManager.setAutoMeasureEnabled(false);
    recyclerViewFriends.setHasFixedSize(true);
    recyclerViewFriends.setLayoutManager(layoutManager);
    recyclerViewFriends.setItemAnimator(null);
    recyclerViewFriends.addItemDecoration(new HomeListDividerDecoration(context(),
        ContextCompat.getColor(context(), R.color.grey_divider), screenUtils.dpToPx(0.5f),
        getSectionCallback(homeGridAdapter.getItems())));
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

    HomeSectionItemDecoration sectionItemDecoration = new HomeSectionItemDecoration(
        getResources().getDimensionPixelSize(R.dimen.list_home_header_height), true,
        getSectionCallback(homeGridAdapter.getItems()), screenUtils);
    recyclerViewFriends.addItemDecoration(sectionItemDecoration);
  }

  private SectionCallback getSectionCallback(final List<HomeAdapterInterface> recipientList) {
    return new SectionCallback() {
      @Override public boolean isSection(int position) {
        if (position < 0 || position > recipientList.size() - 1) return false;
        return position == 0
            || recipientList.get(position).getHomeSectionType() != BaseSectionItemDecoration.NONE
            && recipientList.get(position).getHomeSectionType() != recipientList.get(position - 1)
            .getHomeSectionType();
      }

      @Override public int getSectionType(int position) {
        if (position < 0 || position > recipientList.size() - 1) {
          return BaseSectionItemDecoration.NONE;
        }
        return recipientList.get(position).getHomeSectionType();
      }
    };
  }

  private void displayDeclinedCallNotification(NotificationPayload notificationPayload) {
    LiveNotificationView liveNotificationView =
        NotificationUtils.getNotificationViewFromPayload(this, notificationPayload);
    Alerter.create(HomeActivity.this, liveNotificationView).show();
  }

  @Override public void onShortcutCreatedError() {

  }

  @Override public void onShortcutRemovedSuccess() {

  }

  @Override public void onShortcutRemovedError() {

  }

  @Override public void onShortcutUpdatedSuccess(Shortcut shortcut, BaseListViewHolder viewHolder) {
    for (Shortcut s : user.getShortcutList()) {
      if (s.getId().equals(shortcut.getId())) {
        s.setStatus(shortcut.getStatus());
      }
    }
  }

  @Override public void onShortcutUpdatedError() {

  }

  @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {

  }

  @Override public void onShortcut(Shortcut shortcut) {

  }

  class NotificationReceiverSupport extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      NotificationPayload notificationPayload =
          (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

      if (notificationPayload != null
          && !StringUtils.isEmpty(notificationPayload.getUserId())
          && notificationPayload.getUserId().equals(Shortcut.SUPPORT)
          && supportShortcut != null) {

        supportShortcut.setRead(false);
        supportShortcut.setSingle(true);
        onSupportUpdate.onNext(supportShortcut);
      }
    }
  }
}


