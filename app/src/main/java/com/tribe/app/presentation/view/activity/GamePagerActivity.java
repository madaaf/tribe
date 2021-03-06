package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
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
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.adapter.GamePagerAdapter;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.popup.PopupManager;
import com.tribe.app.presentation.view.popup.listener.PopupDigestListener;
import com.tribe.app.presentation.view.popup.view.PopupDigest;
import com.tribe.app.presentation.view.popup.view.PopupParentView;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.utils.ViewPagerScroller;
import com.tribe.app.presentation.view.widget.CustomViewPager;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameFooter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

import static com.tribe.app.presentation.navigation.Navigator.FROM_GAME_DETAILS;

/**
 * Created by madaaflak on 26/03/2018.
 */

public class GamePagerActivity extends GameActivity implements AppStateListener {

  private static final int PAGER_SPEED = 600;
  private static final float TRANS_IMAGE = 300f;
  private static final float DOT_MAX_SIZE = 1.5f;

  private static final int DURATION = 400;
  private static final int DURATION_MOVING = 2500;
  private static final long TWENTY_FOUR_HOURS = 86400000;
  private static final String FROM_AUTH = "FROM_AUTH";
  private final static String DOTS_TAG_MARKER = "DOTS_TAG_MARKER_";

  // VARIABLES
  private UserComponent userComponent;
  private GamePagerAdapter adapter;
  private PageListener pageListener;
  private Map<String, ValueAnimator> mapAnimator;
  private UserMVPViewAdapter userMVPViewAdapter;
  private Scheduler singleThreadExecutor;
  private AppStateMonitor appStateMonitor;
  private RxPermissions rxPermissions;
  private List<String> userIdsDigest;
  private List<String> roomIdsDigest;
  private List<User> usersChallenge;
  private NotifView notifView;
  private boolean shouldDisplayDigest = true;
  private int previousAnimationValue = 0;
  private GameDetailsView currentGameDetailsView;

  @Inject @LastSyncGameData Preference<Long> lastSyncGameData;
  @Inject @LastSync Preference<Long> lastSync;
  @Inject @ChallengeNotifications Preference<String> challengeNotificationsPref;
  @Inject @DaysOfUsage Preference<Integer> daysOfUsage;
  @Inject @PreviousDateUsage Preference<Long> previousDateUsage;
  @Inject StateManager stateManager;
  @Inject ScreenUtils screenUtils;

  @BindView(R.id.pager) CustomViewPager viewpager;
  @BindView(R.id.dotsContainer) LinearLayout dotsContainer;
  @BindView(R.id.imgAnimation1) ImageView imgAnimation1;
  @BindView(R.id.imgAnimation2) ImageView imgAnimation2;
  @BindView(R.id.imgAnimation3) ImageView imgAnimation3;
  @BindView(R.id.layoutPulse) PulseLayout layoutPulse;
  @BindView(R.id.layoutCall) FrameLayout layoutCall;
  @BindView(R.id.btnFriends) ImageView btnFriends;
  @BindView(R.id.btnNewMessage) ImageView btnNewMessage;
  @BindView(R.id.topbar) FrameLayout topbar;
  @BindView(R.id.btnSingle) FrameLayout btnSingle;
  @BindView(R.id.btnMulti) FrameLayout btnMulti;

  // OBSERVABLES
  private PublishSubject<User> onUser = PublishSubject.create();

  public static Intent getCallingIntent(Activity activity, boolean fromAuth) {
    Intent intent = new Intent(activity, GamePagerActivity.class);
    intent.putExtra(FROM_AUTH, fromAuth);
    return intent;
  }

  private void initViewPager() {
    adapter = new GamePagerAdapter(this);
    pageListener = new PageListener(dotsContainer, this);
    viewpager.setScrollDurationFactor(2);
    viewpager.addOnPageChangeListener(pageListener);
    viewpager.setAdapter(adapter);
    changePagerScroller();
  }

  private void changePagerScroller() {
    try {
      Field mScroller = null;
      mScroller = ViewPager.class.getDeclaredField("mScroller");
      mScroller.setAccessible(true);
      ViewPagerScroller scroller = new ViewPagerScroller(viewpager.getContext(), null, PAGER_SPEED);
      mScroller.set(viewpager, scroller);
    } catch (Exception e) {
      Timber.e("error of change scroller " + e);
    }
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    singleThreadExecutor = Schedulers.from(Executors.newSingleThreadExecutor());
    super.onCreate(savedInstanceState);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    mapAnimator = new HashMap<>();

    if (hasSoftKeys != null && hasSoftKeys.get()) {
      UIUtils.changeBottomMarginOfView(dotsContainer, screenUtils.dpToPx(30));
      UIUtils.changeTopMarginOfView(topbar, screenUtils.dpToPx(20));
    }

    initViewPager();
    if (gameManager.getHomeGames() != null && !gameManager.getHomeGames().isEmpty()) {
      adapter.setItems(gameManager.getHomeGames());
      initUI();
    }

    initParams(getIntent());
    initAppStateMonitor();
    loadChallengeNotificationData();
    computeDaysUsage();
  }

  @Override protected void onResume() {
    super.onResume();
    
    gamePresenter.loadUserLeaderboard(getCurrentUser().getId());
    startService(WSService.
        getCallingIntent(this, null, null));

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          currentGameDetailsView = adapter.getItemAtPosition(0);
          if (currentGameDetailsView != null) currentGameDetailsView.onCurrentViewVisible();
        }));
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
      } else if ((calendarPreviousDate.after(calendarYesterday) || calendarPreviousDate.equals(
          calendarYesterday)) && calendarPreviousDate.before(calendarToday)) {
        nbDays += 1;
      }
    } else {
      nbDays = 1;
    }

    daysOfUsage.set(nbDays);
    previousDateUsage.set(new Date().getTime());
  }

  private void displayFakeSupportNotif() {
    getBroadcastReceiver().notifiyStaticNotifSupport(this);
  }

  private void loadChallengeNotificationData() {
    if (challengeNotificationsPref != null
        && challengeNotificationsPref.get() != null
        && !challengeNotificationsPref.get().isEmpty()) {
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
    //stopDownloadService();
    super.onDestroy();
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

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == FROM_GAME_DETAILS && data != null) {
      String gameId = data.getStringExtra(GamePagerActivity.GAME_ID);
      boolean callRoulette = data.getBooleanExtra(GameMembersActivity.CALL_ROULETTE, false);
      Shortcut shortcut = (Shortcut) data.getSerializableExtra(GameMembersActivity.SHORTCUT);

      if (callRoulette) {
        navigator.navigateToNewCall(this, LiveActivity.SOURCE_CALL_ROULETTE, gameId);
      } else if (shortcut != null) {
        navigator.navigateToLive(this, shortcut, LiveActivity.SOURCE_SHORTCUT_ITEM, gameId, null);
      }
    } else if (requestCode == Navigator.FROM_LIVE) {
      shouldDisplayDigest = false;
      if (notifView != null) notifView.dispose();
    }
  }

  @Override protected void onStart() {
    super.onStart();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
    userPresenter.onViewAttached(userMVPViewAdapter);
    userPresenter.getUserInfos();

    if (System.currentTimeMillis() - lastSync.get() > TWENTY_FOUR_HOURS && rxPermissions.isGranted(
        PermissionUtils.PERMISSIONS_CONTACTS)) {
      userPresenter.syncContacts(lastSync, this);
    }

    if (System.currentTimeMillis() - lastSyncGameData.get() > TWENTY_FOUR_HOURS) {
      Timber.d("Synchronize game data");
      gamePresenter.synchronizeGameData(DeviceUtils.getLanguage(this), lastSyncGameData);
    }
  }

  private void initParams(Intent intent) {
    if (intent != null && intent.hasExtra(FROM_AUTH)) {
      boolean fromExtra = (Boolean) intent.getSerializableExtra(FROM_AUTH);
      if (fromExtra) {
        displayFakeSupportNotif();
        tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_HomeScreen);
      }
    }

    if (getIntent().getData() != null) {
      Intent newIntent = IntentUtils.getLiveIntentFromURI(this, getIntent().getData(),
          LiveActivity.SOURCE_DEEPLINK);
      if (newIntent != null) navigator.navigateToIntent(this, newIntent);
    }

    userIdsDigest = new ArrayList<>();
    roomIdsDigest = new ArrayList<>();
    usersChallenge = new ArrayList<>();

    rxPermissions = new RxPermissions(this);
  }

  private void initUI() {
    onGames.onNext(gameManager.getHomeGames());
    setAnimImageAnimation();
    initDots(gameManager.getHomeGames().size());
    viewpager.setCurrentItem(0);
    GameDetailsView gameDetailsView = adapter.getItemAtPosition(0);
    if (gameDetailsView != null) gameDetailsView.onCurrentViewVisible();

    if (stateManager.shouldDisplay(StateManager.FIRST_SWIPE_GAME_STORE) && adapter.getCount() > 0) {
      subscriptions.add(Observable.timer(5, TimeUnit.SECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            if (viewpager.isFakeDragging()) return;
            viewpager.beginFakeDrag();
            previousAnimationValue = 0;
            ValueAnimator animatorPeekViewPager =
                ValueAnimator.ofInt(0, -screenUtils.dpToPx(30), 0);
            animatorPeekViewPager.setDuration(1000);
            animatorPeekViewPager.setInterpolator(new DecelerateInterpolator());
            animatorPeekViewPager.addUpdateListener(animation -> {
              int value = (int) animation.getAnimatedValue();
              if (viewpager.isFakeDragging()) {
                viewpager.fakeDragBy(-previousAnimationValue + value);
              } else {
                animation.cancel();
              }
              previousAnimationValue = value;
            });
            animatorPeekViewPager.addListener(new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                stateManager.addTutorialKey(StateManager.FIRST_SWIPE_GAME_STORE);
                viewpager.endFakeDrag();
              }
            });
            animatorPeekViewPager.start();
          }));
    }
  }

  private void setImages() {
    for (int i = 0; i < getCurrentGame().getAnimation_icons().size(); i++) {
      String url = getCurrentGame().getAnimation_icons().get(i);
      ImageView imageView = null;

      if (i == 0) {
        imageView = imgAnimation1;
      } else if (i == 1) {
        imageView = imgAnimation2;
      } else if (i == 2) {
        imageView = imgAnimation3;
      }

      Glide.with(this).load(url).into(imageView);
    }
  }

  private void setAnimImageAnimation() {
    setImages();
    animateImg(imgAnimation1, true);
    animateImg(imgAnimation1, false);
    animateImg(imgAnimation2, true);
    animateImg(imgAnimation2, false);
    animateImg(imgAnimation3, true);
    animateImg(imgAnimation3, false);
  }

  private void animateImg(ImageView imgAnimation, boolean isX) {
    int rdm = new Random().nextInt(50) - 25;

    String id = String.valueOf(imgAnimation.getId());
    ValueAnimator animator = mapAnimator.get(id);
    if (animator != null) {
      animator.cancel();
    }

    animator = ValueAnimator.ofInt(0, screenUtils.dpToPx(rdm));
    animator.setDuration(DURATION_MOVING);
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.setRepeatMode(ValueAnimator.REVERSE);
    animator.addUpdateListener(animation -> {
      int translation = (int) animation.getAnimatedValue();
      if (isX) {
        imgAnimation.setTranslationX(translation);
      } else {
        imgAnimation.setTranslationY(translation);
      }
    });
    animator.start();
    mapAnimator.put(id, animator);
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

  @Override protected void initPresenter() {
    super.initPresenter();
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public Context context() {
        return GamePagerActivity.this;
      }

      @Override public void onGameList(List<Game> gameList) {
        List<Game> list = new ArrayList<>();
        for (Game g : gameList) {
          if (g.isIn_home()) list.add(g);
        }
        gameManager.addGames(list);
        adapter.setItems(gameManager.getHomeGames());
        initUI();
        onGames.onNext(gameManager.getHomeGames());
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

  @Override public void onAppDidEnterForeground() {
  }

  @Override public void onAppDidEnterBackground() {
    Timber.d("App in background stopping the service");
    stopService();
  }

  @Override protected int getContentView() {
    return R.layout.activity_game_pagerstore;
  }

  @Override protected void initSubscriptions() {
    super.initSubscriptions();

    subscriptions.add(onUser.onBackpressureBuffer()
        .subscribeOn(singleThreadExecutor)
        .observeOn(AndroidSchedulers.mainThread())
        .map(user -> {
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
                  if (!userIdsDigest.contains(member.getId()) && !roomIdsDigest.contains(
                      member.getId())) {
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

              PopupParentView popupParentView = PopupManager.create(
                  new PopupManager.Builder().activity(this)
                      .dimBackground(false)
                      .listener(new PopupDigestListener() {
                        @Override public void onClick(Recipient recipient) {
                          String userAsk = null;

                          if (recipient instanceof Shortcut) {
                            User user = ((Shortcut) recipient).getSingleFriend();
                            if (user != null) userAsk = user.getId();
                          }

                          navigator.navigateToLive(GamePagerActivity.this, recipient,
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
                  new NotificationModel.Builder().view(popupParentView).build());
            } else {
              shouldDisplayDigest = true;
            }

            if (usersChallenge != null && usersChallenge.size() > 0) {
              notificationModelList.addAll(
                  NotificationUtils.getChallengeNotification(usersChallenge, GamePagerActivity.this,
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
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(view -> {
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

  /**
   * PUBLIC
   */
  @Override public void finish() {
    super.finish();
    for (ValueAnimator animator : mapAnimator.values()) animator.cancel();
    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
  }

  @Override protected void initDependencyInjector() {
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

  private void initDots(int dotsNbr) {
    dotsContainer.removeAllViews();
    int sizeDot = getResources().getDimensionPixelSize(R.dimen.dots_pager_size);
    for (int i = 0; i < dotsNbr; i++) {
      AvatarView v = new AvatarView(this);
      v.load(gameManager.getHomeGames().get(i).getIcon());

      v.setTag(DOTS_TAG_MARKER + i);
      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizeDot, sizeDot);
      lp.setMargins(0, 0, 30, 0);
      lp.gravity = Gravity.CENTER;
      v.setLayoutParams(lp);
      dotsContainer.addView(v);
      if (i == 0) {
        v.setScaleX(DOT_MAX_SIZE);
        v.setScaleY(DOT_MAX_SIZE);
      } else {
        v.setScaleX(1f);
        v.setScaleY(1f);
      }
    }
  }

  public class PageListener extends ViewPager.SimpleOnPageChangeListener {
    private LinearLayout dotsContainer;
    private Context context;
    private int positionViewPager = 0, statePager;
    private float firstValue = 0f;
    private boolean onPageChange = false;

    public PageListener(LinearLayout dotsContainer, Context context) {
      this.dotsContainer = dotsContainer;
      this.context = context;
    }

    public int getPositionViewPage() {
      return positionViewPager;
    }

    private void slideNext(View v, float positionOffset, float transX, float transY) {
      float scale = 1 + (positionOffset * 2);
      v.setScaleX(scale);
      v.setScaleY(scale);
      v.setAlpha(1 - (positionOffset * 3));

      v.setTranslationX(transX);
      v.setTranslationY(transY);
    }

    private void slideBefore(View v, float positionOffset, float transX, float transY) {
      float scale = 1 + ((1 - positionOffset) * 2);
      v.setAlpha(positionOffset * 3);
      v.setTranslationX(transX);
      v.setTranslationY(transY);
      v.setScaleX(scale);
      v.setScaleY(scale);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      super.onPageScrolled(position, positionOffset, positionOffsetPixels);

      if (mapAnimator != null) {
        for (ValueAnimator animator : mapAnimator.values()) animator.cancel();
      }

      if (firstValue == 0f) {
        firstValue = positionOffset;
      }

      if (firstValue < 0.5 && positionOffset > 0.5 && statePager == 1) {
        firstValue = 0f;
      }

      if (firstValue > 0.5 && positionOffset < 0.5 && statePager == 1) {
        firstValue = 0f;
      }

      if (positionOffset != 0f) { // positionOffset from 1 to 0
        if (firstValue > 0.5) {

          if (currentGameDetailsView != null) {
            currentGameDetailsView.slidePager(positionOffset, false);
          }
          float trans = (1 - positionOffset) * screenUtils.dpToPx(TRANS_IMAGE);
          slideBefore(imgAnimation3, positionOffset, trans, trans);
          slideBefore(imgAnimation2, positionOffset, trans, -trans);
          slideBefore(imgAnimation1, positionOffset, -trans, -trans);

          if (positionOffset < 0.5f) {
            firstValue = 0f;
          }

          if (positionOffset > 0.6) {
            setImages();
          }
        } else {  // positionOffset from 0 to 1
          if (currentGameDetailsView != null) {
            currentGameDetailsView.slidePager(positionOffset, true);
          }
          float trans = positionOffset * screenUtils.dpToPx(TRANS_IMAGE);
          slideNext(imgAnimation3, positionOffset, trans, trans);
          slideNext(imgAnimation2, positionOffset, trans, -trans);
          slideNext(imgAnimation1, positionOffset, -trans, -trans);

          if (positionOffset < 0.5f) {
            firstValue = 0f;
          }
          if (positionOffset > 0.3) {
            setImages();
          }
        }
      }
    }

    @Override public void onPageScrollStateChanged(int state) {
      statePager = state;
      if (state == 0f && onPageChange) {
        firstValue = 0f;
        //if (currentGameDetailsView != null) currentGameDetailsView.resetPager();
        setAnimImageAnimation();
      }
      if (state == 2f) {
        onPageChange = false;
      }
    }

    public void onPageSelected(int position) {
      onPageChange = true;
      this.positionViewPager = position;
      positionViewPager = position;
      currentGameDetailsView = adapter.getItemAtPosition(position);
      //if (currentGameDetailsView != null) currentGameDetailsView.resetPager();
      if (currentGameDetailsView != null) currentGameDetailsView.onCurrentViewVisible();
      for (int i = 0; i < dotsContainer.getChildCount(); i++) {
        View v = dotsContainer.getChildAt(i);
        if (v.getTag().toString().startsWith(DOTS_TAG_MARKER + position)) {
          v.animate().scaleX(DOT_MAX_SIZE).scaleY(DOT_MAX_SIZE).setDuration(100).start();
        } else {
          v.setScaleX(1f);
          v.setScaleY(1f);
        }
      }
    }
  }

  private Game getCurrentGame() {
    if (pageListener.getPositionViewPage() > gameManager.getHomeGames().size() - 1) {
      return gameManager.getHomeGames().get(0);
    }

    return gameManager.getHomeGames().get(pageListener.getPositionViewPage());
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

  @OnClick(R.id.btnMulti) void voidOnClickBtnMulti() {
    navigator.navigateToGameMembers(this, getCurrentGame().getId());
  }

  @OnClick(R.id.btnSingle) void voidOnClickBtnSingle() {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.SOURCE, TagManagerUtils.HOME);
    bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.LAUNCHED);
    bundle.putString(TagManagerUtils.NAME, getCurrentGame().getId());
    tagManager.trackEvent(TagManagerUtils.NewGame, bundle);

    navigator.navigateToNewCall(this, LiveActivity.SOURCE_HOME, getCurrentGame().getId());
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
}
