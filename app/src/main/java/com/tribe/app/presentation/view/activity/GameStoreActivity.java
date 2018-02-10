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
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.UserPresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.mvp.view.adapter.UserMVPViewAdapter;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.IntentUtils;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastSyncGameData;
import com.tribe.app.presentation.view.NotificationModel;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.Test;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameFooter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class GameStoreActivity extends GameActivity implements AppStateListener {

  private static final long TWENTY_FOUR_HOURS = 86400000;

  public static Intent getCallingIntent(Activity activity) {
    Intent intent = new Intent(activity, GameStoreActivity.class);
    return intent;
  }

  @Inject UserPresenter userPresenter;
  @Inject @LastSyncGameData Preference<Long> lastSyncGameData;
  @Inject @LastSync Preference<Long> lastSync;
  @Inject User currentUser;

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

    rxPermissions = new RxPermissions(this);
    initAppStateMonitor();
  }

  @Override protected void onStart() {
    super.onStart();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
    userPresenter.onViewAttached(userMVPViewAdapter);
    userPresenter.getUserInfos();

    if (System.currentTimeMillis() - lastSync.get() > TWENTY_FOUR_HOURS && rxPermissions.isGranted(
        PermissionUtils.PERMISSIONS_CONTACTS)) {
      userPresenter.syncContacts(lastSync);
    }

    if (System.currentTimeMillis() - lastSyncGameData.get() > TWENTY_FOUR_HOURS) {
      gamePresenter.synchronizeGameData(DeviceUtils.getLanguage(this), lastSyncGameData);
    }
  }

  @Override protected void onResume() {
    super.onResume();
    gamePresenter.loadUserLeaderboard(getCurrentUser().getId());
    startService(WSService.
        getCallingIntent(this, null, null));
    mock();
  }

  private void mock() {
    Test view = new Test(this);
    List<NotificationModel> list = new ArrayList<>();

    NotificationModel a =
        new NotificationModel.Builder().title(getString(R.string.new_challenger_popup_title))
            .subTitle(getString(R.string.new_challenger_popup_subtitle))
            .content(getString(R.string.new_challenger_popup_friends))
            .btn1Content(getString(R.string.new_challenger_popup_action_add))
            .background(R.drawable.bck_norif_challenge)
            .profilePicture(currentUser.getProfilePicture())
            .build();
    NotificationModel d = new NotificationModel.Builder().title("daia3").build();

    list.add(a);
    list.add(a);
    list.add(a);
    list.add(d);
    view.show(this, list);
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
        navigator.navigateToLive(this, shortcut, LiveActivity.SOURCE_SHORTCUT_ITEM,
            TagManagerUtils.SECTION_SHORTCUT, gameId);
      }
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
    };
  }

  @Override protected void initSubscriptions() {
    super.initSubscriptions();

    subscriptions.add(onUser.onBackpressureBuffer().subscribeOn(singleThreadExecutor).map(user -> {
      boolean hasLive = false, hasNewMessage = false;
      for (Recipient recipient : user.getRecipientList()) {
        if (recipient instanceof Invite) {
          hasLive = true;
        } else if (recipient instanceof Shortcut && !hasNewMessage) {
          hasNewMessage = !recipient.isRead();
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

  /**
   * ONCLICK
   */

  @OnClick({ R.id.btnFriends, R.id.imgLive, R.id.btnNewMessage }) void onClickHome() {
    navigator.navigateToHome(this);
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
