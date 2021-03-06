package com.tribe.app.presentation.view.component.live.game.common;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 02/11/2017.
 */

public class GameEngine {

  @IntDef({ PENDING, PLAYING, GAMEOVER }) public @interface PlayerStatus {
  }

  public static final int PENDING = 0;
  public static final int PLAYING = 1;
  public static final int GAMEOVER = 2;

  @Inject User currentUser;

  // VARIABLES
  protected Context context;
  protected boolean isFirst;
  protected String lastPlayerGameOver;
  protected Map<String, Integer> mapPlayerStatus;

  // OBSERVABLES
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected PublishSubject<String> onPlayerLost = PublishSubject.create();
  protected PublishSubject<String> onPlayerPending = PublishSubject.create();
  protected PublishSubject<String> onPlayerReady = PublishSubject.create();
  protected PublishSubject<String> onGameOver = PublishSubject.create();
  protected BehaviorSubject<Map<String, Integer>> onPlayerStatusChange = BehaviorSubject.create();

  public GameEngine(Context context) {
    this.context = context;
    this.mapPlayerStatus = new HashMap<>();
    initDependencyInjector();
  }

  /**
   * PRIVATE
   */

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) context).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) context));
  }

  protected boolean isGameOver(String userId) {
    List<String> totalPlayers = new ArrayList<>();

    for (String key : mapPlayerStatus.keySet()) {
      if (mapPlayerStatus.get(key) == PLAYING) totalPlayers.add(key);
    }

    if (totalPlayers.size() == 0) {
      onGameOver.onNext(userId);
      return true;
    }

    return false;
  }

  protected void checkGameOver() {
    isGameOver(lastPlayerGameOver);
  }

  /**
   * PUBLIC
   */

  public void start() {

  }

  public void stop() {
    subscriptions.clear();
  }

  public void initPeerMapObservable(Observable<Map<String, TribeGuest>> map) {
    isFirst = true;

    subscriptions.add(map.subscribe(peerMap -> {
      if (isFirst) {
        for (TribeGuest guest : peerMap.values()) {
          mapPlayerStatus.put(guest.getId(), PLAYING);
        }

        mapPlayerStatus.put(currentUser.getId(), PLAYING);
        onPlayerStatusChange.onNext(mapPlayerStatus);
      } else {
        for (TribeGuest guest : peerMap.values()) {
          if (!mapPlayerStatus.containsKey(guest.getId())) {
            mapPlayerStatus.put(guest.getId(), PENDING);
            onPlayerPending.onNext(guest.getId());
          }
        }

        List<String> playersToRemove = new ArrayList<>();
        for (String playerId : mapPlayerStatus.keySet()) {
          if (!peerMap.containsKey(playerId) && !playerId.equals(currentUser.getId())) {
            playersToRemove.add(playerId);
          }
        }

        for (String s : playersToRemove) mapPlayerStatus.remove(s);
      }

      isFirst = false;
    }));
  }

  public Map<String, Integer> getMapPlayerStatus() {
    return mapPlayerStatus;
  }

  public void setUserReady(String userId) {
    Timber.d("setUserReady : " + userId);
    onPlayerReady.onNext(userId);
  }

  public void setUserGameOver(String userId) {
    Timber.d("setUserGameOver : " + userId);
    boolean wasPlaying = mapPlayerStatus.get(userId) == PLAYING;
    mapPlayerStatus.put(userId, GAMEOVER);
    onPlayerStatusChange.onNext(mapPlayerStatus);

    if (!isGameOver(userId) && wasPlaying) onPlayerLost.onNext(userId);
  }

  /**
   * OBSERVABLES
   */

  public Observable<Map<String, Integer>> onPlayerStatusChange() {
    return onPlayerStatusChange;
  }

  public Observable<String> onPlayerReady() {
    return onPlayerReady;
  }
}
