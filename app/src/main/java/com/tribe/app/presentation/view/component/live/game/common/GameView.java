package com.tribe.app.presentation.view.component.live.game.common;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.Unbinder;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
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
  protected String currentMasterId;

  // OBSERVABLES
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected CompositeSubscription subscriptionsRoom = new CompositeSubscription();
  protected Observable<Map<String, TribeGuest>> peerMapObservable;

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

  /**
   * PRIVATE
   */

  protected abstract void initWebRTCRoomSubscriptions();

  private Observable<String> generateNewMasterId() {
    if (game != null) {
      List<String> candidatesIds = new ArrayList<String>();
      candidatesIds.add(currentUser.getId());

      return peerMapObservable.single().flatMap(map -> {
        for (String key : map.keySet()) {
          if (map.get(key).canPlayGames(game.getId())) {
            candidatesIds.add(map.get(key).getId());
          }
        }

        Collections.sort(candidatesIds);
        return Observable.just(candidatesIds.get(0));
      });
    } else {
      return Observable.just(null);
    }
  }

  protected abstract void takeOverGame();

  /**
   * PUBLIC
   */

  public abstract void setNextGame();

  public void start(Observable<Map<String, TribeGuest>> map, String userId) {
    peerMapObservable = map;
    subscriptions.add(map.subscribe(peerMap -> {
      this.peerMap.clear();
      this.peerMap.putAll(peerMap);
    }));
  }

  public void stop() {
    ViewGroup parent = (ViewGroup) getParent();
    if (parent != null) removeView(GameView.this);
    dispose();
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public void setWebRTCRoom(WebRTCRoom webRTCRoom) {
    this.webRTCRoom = webRTCRoom;

    initWebRTCRoomSubscriptions();
  }

  public void dispose() {
    peerMap.clear();
    subscriptionsRoom.clear();
    subscriptions.clear();
  }

  public void userLeft(String userId) {
    if (userId.equals(currentMasterId)) {
      subscriptions.add(generateNewMasterId().subscribe(newMasterId -> {
        if (newMasterId.equals(currentUser.getId())) {
          takeOverGame();
        }
      }));
    }
  }

  /**
   * OBSERVABLE
   */
}
