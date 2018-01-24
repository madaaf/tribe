package com.tribe.app.presentation.view.component.live.game.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.core.WebRTCRoom;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/30/2017.
 */

public abstract class GameView extends FrameLayout {

  protected static final String ACTION_KEY = "action";
  protected static final String USER_KEY = "user";
  protected static final String FROM_KEY = "from";

  @Inject protected User currentUser;
  @Inject protected ScreenUtils screenUtils;
  @Inject protected SoundManager soundManager;

  protected LayoutInflater inflater;
  protected Unbinder unbinder;
  protected Context context;
  protected GameManager gameManager;
  protected WebRTCRoom webRTCRoom;
  protected Game game;
  protected Map<String, TribeGuest> peerMap;
  protected Map<String, LiveStreamView> liveViewsMap;
  protected String currentMasterId;
  protected boolean landscapeMode = false;
  protected TextViewFont txtViewLandscape;

  // OBSERVABLES
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected CompositeSubscription subscriptionsRoom = new CompositeSubscription();
  protected Observable<Map<String, TribeGuest>> peerMapObservable;
  protected PublishSubject<Pair<String, Integer>> onAddScore = PublishSubject.create();
  protected PublishSubject<Game> onRestart = PublishSubject.create();
  protected PublishSubject<Game> onStop = PublishSubject.create();
  protected PublishSubject<Void> onPlayOtherGame = PublishSubject.create();

  public GameView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public GameView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  protected void initView(Context context) {
    this.context = context;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    gameManager = GameManager.getInstance(context);

    peerMap = new HashMap<>();
    liveViewsMap = new HashMap<>();

    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      landscapeMode = true;
    } else {
      landscapeMode = false;
    }
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

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      landscapeMode = true;

      if (txtViewLandscape != null) {
        removeView(txtViewLandscape);
        txtViewLandscape = null;
      }
    } else {
      if (game != null && game.needsLandscape()) showLandscapeToast();
      landscapeMode = false;
    }
  }

  /**
   * PRIVATE
   */

  protected abstract void initWebRTCRoomSubscriptions();

  private Observable<String> generateNewMasterId() {
    if (game != null) {
      List<String> candidatesIds = new ArrayList<>();
      candidatesIds.add(currentUser.getId());

      for (String key : peerMap.keySet()) {
        if (peerMap.get(key).canPlayGames(game.getId())) {
          candidatesIds.add(peerMap.get(key).getId());
        }
      }

      Collections.sort(candidatesIds);
      return Observable.just(candidatesIds.get(0));
    } else {
      return Observable.just(null);
    }
  }

  protected abstract void takeOverGame();

  private void showLandscapeToast() {
    if (txtViewLandscape != null) return;

    txtViewLandscape = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsLandscape =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsLandscape.gravity = Gravity.CENTER;

    TextViewCompat.setTextAppearance(txtViewLandscape, R.style.BiggerTitle_2_White);
    txtViewLandscape.setBackgroundResource(R.drawable.shape_rect_rounded_5_black_70);
    txtViewLandscape.setCustomFont(context, FontUtils.PROXIMA_BOLD);
    txtViewLandscape.setText(R.string.game_rotate_landscape);
    txtViewLandscape.setGravity(Gravity.CENTER);
    txtViewLandscape.setPadding(screenUtils.dpToPx(10), screenUtils.dpToPx(10),
        screenUtils.dpToPx(10), screenUtils.dpToPx(10));
    ViewCompat.setElevation(txtViewLandscape, screenUtils.dpToPx(10));
    addView(txtViewLandscape, paramsLandscape);
  }

  protected interface LabelListener {
    void call();
  }

  /**
   * PUBLIC
   */

  public abstract void setNextGame();

  public void start(Game game, Observable<Map<String, TribeGuest>> map,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    this.game = game;
    this.peerMapObservable = map;

    subscriptions.add(liveViewsObservable.subscribe(stringLiveStreamViewMap -> {
      this.liveViewsMap.clear();
      this.liveViewsMap.putAll(stringLiveStreamViewMap);
    }));

    subscriptions.add(map.subscribe(peerMap -> {
      this.peerMap.clear();
      this.peerMap.putAll(peerMap);
    }));

    if (game.needsLandscape() && !landscapeMode) {
      showLandscapeToast();
    }

    initWebRTCRoomSubscriptions();
  }

  public void stop() {
    dispose();
    ViewGroup parent = (ViewGroup) getParent();
    if (parent != null) removeView(GameView.this);
  }

  public void setWebRTCRoom(WebRTCRoom webRTCRoom) {
    this.webRTCRoom = webRTCRoom;
  }

  public void dispose() {
    peerMap.clear();
    peerMapObservable = null;
    liveViewsMap.clear();
    game = null;
    subscriptionsRoom.unsubscribe();
    subscriptions.unsubscribe();
    if (txtViewLandscape != null) {
      removeView(txtViewLandscape);
      txtViewLandscape = null;
    }
  }

  public void userLeft(String userId) {
    if (userId.equals(currentMasterId)) {
      subscriptions.add(generateNewMasterId().subscribe(newMasterId -> {
        currentMasterId = newMasterId;

        if (newMasterId.equals(currentUser.getId())) {
          takeOverGame();
        }
      }));
    }
  }

  /**
   * OBSERVABLE
   */

  public Observable<Pair<String, Integer>> onAddScore() {
    return onAddScore;
  }

  public Observable<Game> onRestart() {
    return onRestart;
  }

  public Observable<Game> onStop() {
    return onStop;
  }

  public Observable<Void> onPlayOtherGame() {
    return onPlayOtherGame;
  }
}
