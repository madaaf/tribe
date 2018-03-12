package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.OnClick;
import com.f2prateek.rx.preferences.Preference;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.AppStateMonitor;
import com.jenzz.appstate.RxAppStateMonitor;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.data.network.WSService;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.TrophyEnum;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.UserPresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.mvp.view.adapter.UserMVPViewAdapter;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.IntentUtils;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.preferences.ChallengeNotifications;
import com.tribe.app.presentation.utils.preferences.DaysOfUsage;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastSyncGameData;
import com.tribe.app.presentation.utils.preferences.PreviousDateUsage;
import com.tribe.app.presentation.view.NotifView;
import com.tribe.app.presentation.view.NotificationModel;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastSyncGameData;
import com.tribe.app.presentation.view.NotifView;
import com.tribe.app.presentation.view.NotificationModel;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.popup.PopupManager;
import com.tribe.app.presentation.view.popup.listener.PopupDigestListener;
import com.tribe.app.presentation.view.popup.view.PopupDigest;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameFooter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class GameStoreActivity extends GameActivity implements AppStateListener {

  private static final String FROM_AUTH = "FROM_AUTH";
  private static final long TWENTY_FOUR_HOURS = 86400000;

  public static Intent getCallingIntent(Activity activity, boolean fromAuth) {
    Intent intent = new Intent(activity, GameStoreActivity.class);
    intent.putExtra(FROM_AUTH, fromAuth);
    return intent;
  }

  @Inject UserPresenter userPresenter;

  @Inject @LastSyncGameData Preference<Long> lastSyncGameData;

  @Inject @LastSync Preference<Long> lastSync;
  
  @Inject @ChallengeNotifications Preference<String> challengeNotificationsPref;
  
  @Inject @DaysOfUsage Preference<Integer> daysOfUsage;
  
  @Inject @PreviousDateUsage Preference<Long> previousDateUsage;
  
  @Inject User currentUser;
  
  @Inject StateManager stateManager;

  @Inject PaletteGrid paletteGrid;

  @BindView(R.id.layoutPulse) PulseLayout layoutPulse;
  @BindView(R.id.layoutCall) FrameLayout layoutCall;
  @BindView(R.id.btnFriends) ImageView btnFriends;
  @BindView(R.id.btnNewMessage) ImageView btnNewMessage;

  // VARIABLES
  private UserComponent userComponent;
  private UserMVPViewAdapter userMVPViewAdapter;
  private Scheduler singleThreadExecutor;
  private AppStateMonitor appStateMonitor;
  private RxPermissions rxPermissions;
  private List<String> userIdsDigest;
  private List<String> roomIdsDigest;
  private List<User> usersChallenge;
  private NotifView notifView;
  private boolean shouldDisplayDigest = true;

  // RESOURCES

  // OBSERVABLES
  private PublishSubject<User> onUser = PublishSubject.create();

  @Override protected void onCreate(Bundle savedInstanceState) {
    singleThreadExecutor = Schedulers.from(Executors.newSingleThreadExecutor());

    super.onCreate(savedInstanceState);

    if (getIntent().getData() != null) {
      Intent newIntent = IntentUtils.getLiveIntentFromURI(this, getIntent().getData(),
          LiveActivity.SOURCE_DEEPLINK);
      if (newIntent != null) navigator.navigateToIntent(this, newIntent);
    }

    userIdsDigest = new ArrayList<>();
    roomIdsDigest = new ArrayList<>();
    usersChallenge = new ArrayList<>();

    rxPermissions = new RxPermissions(this);

    initParams(getIntent());
    initAppStateMonitor();
    loadChallengeNotificationData();
    computeDaysUsage();
  }

  private void initParams(Intent intent) {
    if (intent != null && intent.hasExtra(FROM_AUTH)) {
      boolean fromExtra = (Boolean) intent.getSerializableExtra(FROM_AUTH);
      if (fromExtra) displayFakeSupportNotif();
    }
  }

  @Override protected void onStart() {
    super.onStart();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
    userPresenter.onViewAttached(userMVPViewAdapter);
    userPresenter.getUserInfos();

    if (System.currentTimeMillis() - lastSync.get() > TWENTY_FOUR_HOURS &&
        rxPermissions.isGranted(PermissionUtils.PERMISSIONS_CONTACTS)) {
      userPresenter.syncContacts(lastSync);
    }

    if (System.currentTimeMillis() - lastSyncGameData.get() > TWENTY_FOUR_HOURS) {
      Timber.d("Synchronize game data");
      gamePresenter.synchronizeGameData(DeviceUtils.getLanguage(this), lastSyncGameData);
    }
  }

  @Override protected void onResume() {
    super.onResume();
    gamePresenter.loadUserLeaderboard(getCurrentUser().getId());
    startService(WSService.
        getCallingIntent(this, null, null));
  }

  private void displayFakeSupportNotif() {
    getBroadcastReceiver().notifiyStaticNotifSupport(this);
  }

  private void loadChallengeNotificationData() {
    if (challengeNotificationsPref != null &&
        challengeNotificationsPref.get() != null &&
        !challengeNotificationsPref.get().isEmpty()) {
      ArrayList usersIds =
          new ArrayList<>(Arrays.asList(challengeNotificationsPref.get().split(",")));
      userPresenter.getUsersInfoListById(usersIds);
    }
  }

  @Override protected void onStop() {
    super.onStop();
    userPresenter.onViewDetached();
    gamePresenter.onViewDetached();
    layoutPulse.stop();
  }

  @Override protected void onDestroy() {
    if (appStateMonitor != null) {
      appStateMonitor.removeListener(this);
      appStateMonitor.stop();
    }

    stopService();
    super.onDestroy();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Navigator.FROM_GAMESTORE && data != null) {
      String gameId = data.getStringExtra(GameStoreActivity.GAME_ID);
      boolean callRoulette = data.getBooleanExtra(GameMembersActivity.CALL_ROULETTE, false);
      Shortcut shortcut = (Shortcut) data.getSerializableExtra(GameMembersActivity.SHORTCUT);

      if (callRoulette) {
        navigator.navigateToNewCall(this, LiveActivity.SOURCE_HOME, gameId);
      } else if (shortcut != null) {
        navigator.navigateToLive(this, shortcut, LiveActivity.SOURCE_SHORTCUT_ITEM, gameId, null);
      }
    } else if (requestCode == Navigator.FROM_LIVE) {
      shouldDisplayDigest = false;
      if (notifView != null) notifView.dispose();
    }
  }

  private void stopService() {
    Intent i = new Intent(this, WSService.class);
    stopService(i);
  }

  private void initAppStateMonitor() {
    appStateMonitor = RxAppStateMonitor.create(getApplication());
    appStateMonitor.addListener(this);
    appStateMonitor.start();
  }

  @Override protected void initPresenter() {
    super.initPresenter();
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public Context context() {
        return GameStoreActivity.this;
      }

      @Override public void onGameList(List<Game> gameList) {
        gameManager.addGames(gameList);
        onGames.onNext(gameList);
      }
    };

    gamePresenter.getGames();

    userMVPViewAdapter = new UserMVPViewAdapter() {
      @Override public void onUserInfos(User user) {
        onUser.onNext(user);
      }

      @Override public void onUserInfosList(List<User> users) {
        usersChallenge = users;
      }

      @Override public void onUserRefreshDone() {
        String trophy = user.getTrophy();
        TrophyEnum currentTrophy = TrophyEnum.getTrophyEnum(trophy);
        List<TrophyEnum> trophies = TrophyEnum.getTrophies();

        for (TrophyEnum te : trophies) {
          if (trophies.indexOf(te) > trophies.indexOf(currentTrophy) && te.isAchieved()) {
            userPresenter.updateUserTrophy(te.getTrophy());
            user.setTrophy(trophy);
            displayNotificationNewTrophy(te);
          }
        }
      }
    };
  }

  @Override protected void initSubscriptions() {
    super.initSubscriptions();

    subscriptions.add(onUser.onBackpressureBuffer().subscribeOn(singleThreadExecutor).observeOn(AndroidSchedulers.mainThread()).map(user -> {
      boolean hasLive = false, hasNewMessage = false;
      List<HomeAdapterInterface> items = new ArrayList<>();
      for (Recipient recipient : user.getRecipientList()) {
        if (recipient instanceof Invite) {
          Invite invite = (Invite) recipient;
          hasLive = true;
          if (!roomIdsDigest.contains(invite.getRoom().getId())) {
            roomIdsDigest.add(invite.getRoom().getId());
            items.add(recipient);
          }
        } else if (recipient instanceof Shortcut) {
          Shortcut shortcut = (Shortcut) recipient;
          if (!hasNewMessage) hasNewMessage = !recipient.isRead();
          if (shortcut.isSingle()) {
            User member = shortcut.getSingleFriend();
            if (member.isPlayingAGame()) {
              if (!userIdsDigest.contains(member.getId()) &&
                  !roomIdsDigest.contains(member.getId())) {
                userIdsDigest.add(member.getId());
                items.add(shortcut);
              }
            }
          }
        }
      }

      if (!NotifView.isDisplayed()) {
        List<NotificationModel> notificationModelList = new ArrayList<>();

        if (items.size() > 0 && shouldDisplayDigest) {
          PopupDigest popupDigest =
              (PopupDigest) getLayoutInflater().inflate(R.layout.view_popup_digest, null);
          popupDigest.setItems(items);

          PopupManager popupManager = PopupManager.create(new PopupManager.Builder().activity(this)
              .dimBackground(false)
              .listener(new PopupDigestListener() {
                @Override public void onClick(Recipient recipient) {
                  String userAsk = null;

                  if (recipient instanceof Shortcut) {
                    User user = ((Shortcut) recipient).getSingleFriend();
                    if (user != null) userAsk = user.getId();
                  }

                  navigator.navigateToLive(GameStoreActivity.this, recipient,
                      recipient instanceof Invite ? LiveActivity.SOURCE_DRAGGED_AS_GUEST
                          : LiveActivity.SOURCE_GRID, null, userAsk);
                  if (notifView != null) notifView.dispose();
                }

                @Override public void onClickMore() {
                  onClickHome();
                }
              })
              .view(popupDigest));

          notificationModelList.add(
              new NotificationModel.Builder().view(popupManager.getView()).build());
        } else {
          shouldDisplayDigest = true;
        }

        if (usersChallenge != null && usersChallenge.size() > 0) {
          notificationModelList.addAll(
              NotificationUtils.getChallengeNotification(usersChallenge, GameStoreActivity.this,
                  stateManager, user, challengeNotificationsPref));
          usersChallenge = null;
        }

        if (notificationModelList.size() > 0) {
          if (notifView != null) {
            notifView.dispose();
            notifView = null;
          }

          notifView = new NotifView(this);
          notifView.show(this, notificationModelList);
        }
      }

      if (hasLive) {
        return layoutCall;
      } else if (hasNewMessage) {
        return btnNewMessage;
      } else {
        return btnFriends;
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(view -> {
      if (view == layoutCall) {
        layoutCall.setVisibility(View.VISIBLE);
        layoutPulse.start();
        btnFriends.setVisibility(View.GONE);
        btnNewMessage.setVisibility(View.GONE);
      } else if (view == btnFriends) {
        layoutCall.setVisibility(View.GONE);
        layoutPulse.stop();
        btnFriends.setVisibility(View.VISIBLE);
        btnNewMessage.setVisibility(View.GONE);
      } else if (view == btnNewMessage) {
        layoutCall.setVisibility(View.GONE);
        layoutPulse.stop();
        btnFriends.setVisibility(View.GONE);
        btnNewMessage.setVisibility(View.VISIBLE);
      }
    }));
  }

  @Override protected void onGameSelected(Game game) {
    if (game instanceof GameFooter) {
      if (game.getId().equals(Game.GAME_SUPPORT)) {
        Shortcut s = ShortcutUtil.createShortcutSupport();
        s.setTypeSupport(Conversation.TYPE_SUGGEST_GAME);
        navigator.navigateToChat(this, s, null, null, false);
        Bundle bundle = new Bundle();
        bundle.putString(TagManagerUtils.SOURCE, TagManagerUtils.NEW_GAME);
        bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.SUGGESTED);
        tagManager.trackEvent(TagManagerUtils.NewGame, bundle);
      }
    } else {
      navigator.navigateToGameDetails(this, game.getId());
    }
  }

  @Override protected int getContentView() {
    return R.layout.activity_game_store;
  }

  protected void initDependencyInjector() {
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

  @Override public void onAppDidEnterForeground() {
  }

  @Override public void onAppDidEnterBackground() {
    Timber.d("App in background stopping the service");
    stopService();
  }

  private void computeDaysUsage() {
    int nbDays = daysOfUsage.get();
    long previousDateMilli = previousDateUsage.get();

    if (previousDateMilli > 0) {
      Date previousDate = new Date(previousDateMilli);

      Calendar calendarToday = Calendar.getInstance();
      calendarToday.set(Calendar.HOUR_OF_DAY, 0);
      calendarToday.set(Calendar.MINUTE, 0);
      calendarToday.set(Calendar.SECOND, 0);
      calendarToday.set(Calendar.MILLISECOND, 0);

      Calendar calendarYesterday = Calendar.getInstance();
      calendarYesterday.set(Calendar.HOUR_OF_DAY, 0);
      calendarYesterday.set(Calendar.MINUTE, 0);
      calendarYesterday.set(Calendar.SECOND, 0);
      calendarYesterday.set(Calendar.MILLISECOND, 0);
      calendarYesterday.add(Calendar.DATE, -1);

      Calendar calendarPreviousDate = Calendar.getInstance();
      calendarPreviousDate.setTime(previousDate);

      if (calendarPreviousDate.before(calendarYesterday)) {
        nbDays = 1;
      } else if ((calendarPreviousDate.after(calendarYesterday) ||
          calendarPreviousDate.equals(calendarYesterday)) &&
          calendarPreviousDate.before(calendarToday)) {
        nbDays += 1;
      }
    } else {
      nbDays = 1;
    }

    daysOfUsage.set(nbDays);
    previousDateUsage.set(new Date().getTime());
  }

  private void displayNotificationNewTrophy(TrophyEnum te) {
    NotificationPayload notificationPayload = new NotificationPayload();
    notificationPayload.setSound("game_friend_leader.ogg");
    notificationPayload.setTitle(getString(R.string.leaderboards_title));
    notificationPayload.setUserId(user.getId());
    notificationPayload.setUserDisplayName(user.getDisplayName());
    notificationPayload.setUserPicture(user.getProfilePicture());
    notificationPayload.setBody(EmojiParser.demojizedText(
        getString(R.string.trophy_notification_message, getString(te.getTitle()))));
    notificationPayload.setClickAction(NotificationPayload.CLICK_ACTION_NEW_TROPHY);
    notificationReceiver.computeNotificationPayload(this, notificationPayload);
  }

  /**
   * ONCLICK
   */

  @OnClick({ R.id.btnFriends, R.id.imgLive, R.id.btnNewMessage }) void onClickHome() {
    navigator.navigateToHome(this);
    if (notifView != null) notifView.dispose();
  }

  @OnClick(R.id.btnLeaderboards) void onClickLeaderboards() {
    navigator.navigateToLeaderboards(this, getCurrentUser());
  }

  /**
   * PUBLIC
   */

  /**
   * OBSERVABLES
   */
}