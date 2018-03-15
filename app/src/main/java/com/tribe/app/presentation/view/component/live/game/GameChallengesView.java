package com.tribe.app.presentation.view.component.live.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameView;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.utils.ViewPagerScroller;
import com.tribe.app.presentation.view.widget.game.GameChallengeViewPagerAdapter;
import com.tribe.app.presentation.view.widget.game.GameViewPager;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameChallenge;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengesView extends GameView {

  private static int DURATION_EXIT_POPUP = 300;

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.pager) GameViewPager viewpager;
  @BindView(R.id.popupChallenge) FrameLayout popup;

  // VARIABLES
  private GameChallengeViewPagerAdapter adapter;
  private boolean popupDisplayed = false;
  private GameChallenge gameChallenge;
  private GameMVPViewAdapter gameMVPViewAdapter;

  // OBSERVABLES
  private PublishSubject<GameChallenge> onNextChallenge = PublishSubject.create();
  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();

  public GameChallengesView(@NonNull Context context) {
    super(context);
  }

  public GameChallengesView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);
    inflater.inflate(R.layout.view_game_challenges, this, true);
    unbinder = ButterKnife.bind(this);

    adapter = new GameChallengeViewPagerAdapter(context, currentUser);
    viewpager.setAdapter(adapter);

    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public void onGameData(List<String> data) {
        gameChallenge.setDataList(data);
        Timber.d("Data loaded");
        setupNewChallenge();
      }
    };
    gamePresenter.onViewAttached(gameMVPViewAdapter);

    viewpager.setOnTouchListener((v, event) -> {
      if (popupDisplayed) hidePopup();
      return true;
    });
    changePagerScroller();

    onBlockOpenInviteView = PublishSubject.create();
    onNextChallenge = PublishSubject.create();

    subscriptions.add(adapter.onBlockOpenInviteView().subscribe(onBlockOpenInviteView));
    subscriptions.add(adapter.onCurrentGame().subscribe(game -> {
      Timber.d("onCurrentGame");
      GameChallenge gameChallenge = (GameChallenge) game;
      webRTCRoom.sendToPeers(
          getNewChallengePayload(currentUser.getId(), gameChallenge.getCurrentChallenger().getId(),
              gameChallenge.getCurrentChallenge()), false);
      //onNextChallenge.onNext(gameChallenge);
    }));
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    gamePresenter.onViewDetached();
  }

  @Override public void dispose() {
    super.dispose();
    adapter = null;
  }

  @Override public void stop() {
    super.stop();
    gameChallenge = null;
    gameMVPViewAdapter = null;
  }

  @Override protected void takeOverGame() {
    Timber.d("takeOverGame");
    nextChallenge();
  }

  @Override public void setNextGame() {
    Timber.d("setNextGame");
    nextChallenge();
  }

  @Override public void start(Game game,
      Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> masterMapObs,
      Observable<Map<String, TribeGuest>> map, Observable<Map<String, TribeGuest>> mapInvited,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    super.start(game, masterMapObs, map, mapInvited, liveViewsObservable, userId);
    gameChallenge = (GameChallenge) game;
    currentMasterId = userId;
    game.setCurrentMaster(peerMap.get(userId));
    Timber.d("start");
  }

  private void nextChallenge() {
    Timber.d("nextChallenge");
    if (game != null) game.incrementRoundCount();
    if (adapter == null) initView(context);
    if (popupDisplayed) hidePopup();

    if (gameChallenge.getDataList() == null || gameChallenge.getDataList().size() == 0) {
      Timber.d("no data, loading data");
      gamePresenter.synchronizeGame(DeviceUtils.getLanguage(getContext()), gameChallenge.getId());
    } else {
      setupNewChallenge();
    }
  }

  private void setupNewChallenge() {
    Timber.d("setupNewChallenge");
    new Handler().post(() -> {
      int currentItem = (viewpager.getCurrentItem() + 1);
      viewpager.setCurrentItem(currentItem);
      setVisibility(VISIBLE);
    });
  }

  private JSONObject getNewChallengePayload(String userId, String peerId, String challengeMessage) {
    JSONObject app = new JSONObject();
    JSONObject obj = new JSONObject();
    JSONObject challenge = new JSONObject();
    JsonUtils.jsonPut(challenge, "from", userId);
    JsonUtils.jsonPut(challenge, Game.ACTION, Game.NEW_CHALLENGE);
    JsonUtils.jsonPut(challenge, "user", peerId);
    JsonUtils.jsonPut(challenge, Game.CHALLENGE, challengeMessage);
    JsonUtils.jsonPut(obj, Game.GAME_CHALLENGE, challenge);
    JsonUtils.jsonPut(app, "app", obj);
    return app;
  }

  private void changePagerScroller() {
    try {
      Field mScroller = null;
      mScroller = ViewPager.class.getDeclaredField("mScroller");
      mScroller.setAccessible(true);
      ViewPagerScroller scroller =
          new ViewPagerScroller(viewpager.getContext(), new OvershootInterpolator(1.3f));
      mScroller.set(viewpager, scroller);
    } catch (Exception e) {
      Timber.e("error of change scroller " + e);
    }
  }

  private void hidePopup() {
    popupDisplayed = false;
    ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(popup, "scaleX", 0f);
    ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(popup, "scaleY", 0f);
    scaleDownX.setDuration(DURATION_EXIT_POPUP);
    scaleDownY.setDuration(DURATION_EXIT_POPUP);

    AnimatorSet scaleDown = new AnimatorSet();
    scaleDown.play(scaleDownX).with(scaleDownY);
    scaleDownX.addUpdateListener(valueAnimator -> {
      float value = (float) valueAnimator.getAnimatedValue();
      popup.setAlpha(value);
    });

    scaleDownX.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        popup.setVisibility(View.INVISIBLE);
      }
    });
    scaleDown.start();
  }

  public void displayPopup() {
    popup.setTranslationY(
        -context.getResources().getDimensionPixelSize(R.dimen.game_tooltip_first_height) +
            screenUtils.dpToPx(-82));

    popup.setVisibility(VISIBLE);
    popup.setAlpha(1);
    popupDisplayed = true;
    SpringSystem springSystem = SpringSystem.create();
    Spring spring = springSystem.createSpring();
    SpringConfig config = new SpringConfig(400, 20);
    spring.setSpringConfig(config);

    spring.addListener(new SimpleSpringListener() {
      @Override public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();
        popup.setScaleX(value);
        popup.setScaleY(value);
      }
    });
    spring.setEndValue(1);
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    subscriptions.add(webRTCRoom.onNewChallengeReceived()
        .onBackpressureLatest()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(datas -> {
          TribeGuest guestChallenged = null;

          for (TribeGuest guest : peerMap.values()) {
            if (guest.getId().equals(datas.get(1))) {
              guestChallenged = guest;
            }
          }

          gameManager.setCurrentDataGame(datas.get(0), guestChallenged);

          GameChallenge gameChallenge = (GameChallenge) gameManager.getCurrentGame();
          if (gameChallenge.hasDatas()) setNextGame();
        }));
  }

  /**
   * OBSERVABLES
   */

  public Observable<GameChallenge> onNextChallenge() {
    return onNextChallenge;
  }
}
