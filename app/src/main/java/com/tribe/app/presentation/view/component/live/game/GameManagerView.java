package com.tribe.app.presentation.view.component.live.game;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.preferences.GameData;
import com.tribe.app.presentation.view.component.live.game.AliensAttack.GameAliensAttackView;
import com.tribe.app.presentation.view.component.live.game.common.GameView;
import com.tribe.tribelivesdk.core.WebRTCRoom;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/30/2017.
 */

public class GameManagerView extends FrameLayout {

  @Inject @GameData Preference<String> gameData;

  @Inject User currentUser;

  /**
   * VARIABLES
   */

  private Unbinder unbinder;
  private GameManager gameManager;
  private GameView currentGameView;
  private WebRTCRoom webRTCRoom;
  private Map<String, TribeGuest> peerMap;
  private Game currentGame;
  private Map<String, List<String>> mapGameData;

  /**
   * OBSERVABLES
   */

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private BehaviorSubject<Map<String, TribeGuest>> onPeerMapChange = BehaviorSubject.create();
  private PublishSubject<Game> onRestartGame = PublishSubject.create();

  public GameManagerView(@NonNull Context context) {
    super(context);
    initView();
  }

  public GameManagerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  private void initView() {
    initDependencyInjector();
    unbinder = ButterKnife.bind(this);
    gameManager = GameManager.getInstance(getContext());
    peerMap = new HashMap<>();

    if (!StringUtils.isEmpty(gameData.get())) {
      mapGameData = new Gson().fromJson(gameData.get(), new TypeToken<HashMap<String, Object>>() {
      }.getType());
    } else {
      mapGameData = new HashMap<>();
    }

    initSubscriptions();
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

  private void initSubscriptions() {
    subscriptions.add(Observable.merge(gameManager.onCurrentUserStartGame()
        .map(game -> Pair.create(new TribeSession(TribeSession.PUBLISHER_ID, currentUser.getId()),
            game)), gameManager.onRemoteUserStartGame())
        .filter(sessionPairgame -> sessionPairgame.second.hasView())
        .subscribe(sessionGamePair -> {
          currentGame = sessionGamePair.second;

          if (currentGameView == null) {
            setVisibility(View.VISIBLE);
            addGameView(computeGameView(currentGame, sessionGamePair.first.getUserId()));
          }

          if (currentGameView instanceof GameChallengesView) {
            GameChallengesView gameChallengesView = (GameChallengesView) currentGameView;
            gameChallengesView.displayPopup();
          }

          currentGameView.setNextGame();
        }));

    subscriptions.add(Observable.merge(gameManager.onCurrentUserNewSessionGame(),
        gameManager.onRemoteUserNewSessionGame()
            .map(tribeSessionGamePair -> tribeSessionGamePair.second))
        .filter(game -> game.hasView())
        .subscribe(game -> {

        }));

    subscriptions.add(Observable.merge(gameManager.onCurrentUserStopGame(),
        gameManager.onRemoteUserStopGame().map(tribeSessionGamePair -> tribeSessionGamePair.second))
        .filter(game -> game.hasView())
        .subscribe(game -> {
          currentGameView.stop();
          removeView(currentGameView);
          currentGameView = null;
          currentGame = null;
          setVisibility(View.GONE);
        }));
  }

  public void initPeerGuestObservable(
      Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> obs) {
    peerMap.put(currentUser.getId(), currentUser.asTribeGuest());
    onPeerMapChange.onNext(peerMap);

    subscriptions.add(obs.subscribe(rxHashMapAction -> {
      if (rxHashMapAction.changeType.equals(ObservableRxHashMap.ADD)) {
        peerMap.put(rxHashMapAction.item.getId(), rxHashMapAction.item);
      } else if (rxHashMapAction.changeType.equals(ObservableRxHashMap.REMOVE)) {
        peerMap.remove(rxHashMapAction.item.getId());
      }

      onPeerMapChange.onNext(peerMap);
    }));
  }

  private void addGameView(GameView currentGameView) {
    this.currentGameView = currentGameView;
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    addView(this.currentGameView, params);
  }

  private GameView computeGameView(Game game, String userId) {
    GameView gameView = null;

    if (game.getId().equals(Game.GAME_CHALLENGE)) {
      GameChallengesView gameChallengesView = new GameChallengesView(getContext());
      gameView = gameChallengesView;
      subscriptions.add(gameChallengesView.onNextChallenge().subscribe(onRestartGame));
    } else if (game.getId().equals(Game.GAME_DRAW)) {
      GameDrawView gameDrawView = new GameDrawView(getContext());
      gameView = gameDrawView;
      subscriptions.add(gameDrawView.onNextDraw()
          .map(aBoolean -> gameManager.getCurrentGame())
          .subscribe(onRestartGame));
    } else if (game.getId().equals(Game.GAME_INVADERS)) {
      GameAliensAttackView gameAlienAttacksView = new GameAliensAttackView(getContext());
      gameView = gameAlienAttacksView;
      gameView.start(onPeerMapChange, userId);
    }

    gameView.setWebRTCRoom(webRTCRoom);
    gameView.setGame(game);
    game.initPeerMapObservable(onPeerMapChange);
    game.setDataList(mapGameData.get(game.getId()));

    return gameView;
  }

  /**
   * PUBLIC
   */

  public void setWebRTCRoom(WebRTCRoom webRTCRoom) {
    this.webRTCRoom = webRTCRoom;
  }

  public void dispose() {
    subscriptions.clear();
  }

  /**
   * OBSERVABLES
   */

  public Observable<Game> onRestartGame() {
    return onRestartGame;
  }
}
