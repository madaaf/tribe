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
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.preferences.GameData;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.battlemusic.GameBattleMusicView;
import com.tribe.app.presentation.view.component.live.game.birdrush.GameBirdRushView;
import com.tribe.app.presentation.view.component.live.game.common.GameView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithRanking;
import com.tribe.app.presentation.view.component.live.game.corona.GameCoronaView;
import com.tribe.app.presentation.view.component.live.game.trivia.GameTriviaView;
import com.tribe.app.presentation.view.component.live.game.web.GameWebView;
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
import timber.log.Timber;

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
  private Map<String, TribeGuest> invitedMap;
  private Game currentGame;
  private Map<String, List<String>> mapGameData;
  private Observable<Map<String, LiveStreamView>> onLiveViewsChange;

  /**
   * OBSERVABLES
   */
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsGame = new CompositeSubscription();
  private BehaviorSubject<Map<String, TribeGuest>> onPeerMapChange = BehaviorSubject.create();
  private BehaviorSubject<Map<String, TribeGuest>> onInvitedMapChange = BehaviorSubject.create();
  private PublishSubject<Game> onRestartGame = PublishSubject.create();
  private PublishSubject<Game> onStopGame = PublishSubject.create();
  private PublishSubject<Void> onPlayOtherGame = PublishSubject.create();
  private PublishSubject<Pair<String, Integer>> onAddScore = PublishSubject.create();
  private PublishSubject<GameCoronaView> onRevive = PublishSubject.create();
  private Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> masterMapObs;

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

    setId(View.generateViewId());

    gameManager = GameManager.getInstance(getContext());
    peerMap = new HashMap<>();
    invitedMap = new HashMap<>();

    if (!StringUtils.isEmpty(gameData.get())) {
      mapGameData = new Gson().fromJson(gameData.get(), new TypeToken<HashMap<String, Object>>() {
      }.getType());
    } else {
      mapGameData = new HashMap<>();
    }

    setBackground(null);

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
            addGameView(computeGameView(currentGame, sessionGamePair.first.getUserId()));
            return;
          }

          if (currentGameView instanceof GameChallengesView) {
            GameChallengesView gameChallengesView = (GameChallengesView) currentGameView;
            gameChallengesView.displayPopup();
          }
        }));

    subscriptions.add(Observable.merge(gameManager.onCurrentUserNewSessionGame(),
        gameManager.onRemoteUserNewSessionGame()
            .map(tribeSessionGamePair -> tribeSessionGamePair.second)).doOnNext(game1 -> {
      Timber.d("onRemoteUserNewSessionGame");
      if (currentGameView != null) {
        if (currentGameView instanceof GameDrawView) {
          GameDrawView gameDrawView = (GameDrawView) currentGameView;
          gameDrawView.setNextGame();
        } else if (currentGameView instanceof GameChallengesView) {
          GameChallengesView gameChallengesView = (GameChallengesView) currentGameView;
          gameChallengesView.setNextGame();
        }
      }
    }).subscribe());

    subscriptions.add(Observable.merge(gameManager.onCurrentUserStopGame(),
        gameManager.onRemoteUserStopGame().map(tribeSessionGamePair -> tribeSessionGamePair.second))
        .filter(game -> game.hasView())
        .subscribe(game -> {
          if (currentGameView != null) {
            currentGameView.stop();
            removeView(currentGameView);
          }

          currentGameView = null;
          currentGame = null;
        }));

    subscriptions.add(gameManager.onCurrentUserResetScores().subscribe(game -> {
      if (currentGameView != null && currentGameView instanceof GameViewWithRanking) {
        ((GameViewWithRanking) currentGameView).resetScores(true);
      }
    }));

    subscriptions.add(gameManager.onGamePlayedNotAvailable()
        .subscribe(
            aVoid -> Toast.makeText(getContext(), R.string.game_unavailable, Toast.LENGTH_SHORT)
                .show()));
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
        if (currentGameView != null) currentGameView.userLeft(rxHashMapAction.item.getId());
      } else if (rxHashMapAction.changeType.equals(ObservableRxHashMap.CLEAR)) {
        peerMap.clear();
      }

      onPeerMapChange.onNext(peerMap);
    }));
  }

  public void initInvitedGuestObservable(
      Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> obs) {
    invitedMap.put(currentUser.getId(), currentUser.asTribeGuest());
    onInvitedMapChange.onNext(invitedMap);
    masterMapObs = obs;

    subscriptions.add(obs.subscribe(rxHashMapAction -> {
      if (rxHashMapAction.changeType.equals(ObservableRxHashMap.ADD)) {
        invitedMap.put(rxHashMapAction.item.getId(), rxHashMapAction.item);
      } else if (rxHashMapAction.changeType.equals(ObservableRxHashMap.REMOVE)) {
        invitedMap.remove(rxHashMapAction.item.getId());
      } else if (rxHashMapAction.changeType.equals(ObservableRxHashMap.CLEAR)) {
        invitedMap.clear();
      }

      onInvitedMapChange.onNext(invitedMap);
    }));
  }

  public void initLiveViewsObservable(Observable<Map<String, LiveStreamView>> observable) {
    this.onLiveViewsChange = observable;
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
      subscriptionsGame.add(gameChallengesView.onNextChallenge().subscribe(onRestartGame));
    } else if (game.getId().equals(Game.GAME_DRAW)) {
      GameDrawView gameDrawView = new GameDrawView(getContext());
      gameView = gameDrawView;
      subscriptionsGame.add(gameDrawView.onNextDraw()
          .map(aBoolean -> gameManager.getCurrentGame())
          .subscribe(onRestartGame));
    } else if (game.getId().equals(Game.GAME_INVADERS)) {
      GameCoronaView gameCoronaView = new GameCoronaView(getContext(), game);
      subscriptionsGame.add(gameCoronaView.onAddScore().subscribe(onAddScore));
      subscriptionsGame.add(gameCoronaView.onRevive().subscribe(onRevive));
      gameView = gameCoronaView;
    } else if (game.getId().equals(Game.GAME_TRIVIA)) {
      GameTriviaView gameTriviaView = new GameTriviaView(getContext());
      subscriptionsGame.add(gameTriviaView.onAddScore().subscribe(onAddScore));
      subscriptionsGame.add(gameTriviaView.onStop().subscribe(onStopGame));
      subscriptionsGame.add(gameTriviaView.onRestart().subscribe(onRestartGame));
      subscriptionsGame.add(gameTriviaView.onPlayOtherGame()
          .doOnNext(aVoid -> onStopGame.onNext(currentGame))
          .subscribe(onPlayOtherGame));
      gameView = gameTriviaView;
    } else if (game.getId().equals(Game.GAME_BATTLE_MUSIC)) {
      GameBattleMusicView gameBattleMusicView = new GameBattleMusicView(getContext());
      subscriptionsGame.add(gameBattleMusicView.onAddScore().subscribe(onAddScore));
      subscriptionsGame.add(gameBattleMusicView.onStop().subscribe(onStopGame));
      subscriptionsGame.add(gameBattleMusicView.onRestart().subscribe(onRestartGame));
      subscriptionsGame.add(gameBattleMusicView.onPlayOtherGame()
          .doOnNext(aVoid -> onStopGame.onNext(currentGame))
          .subscribe(onPlayOtherGame));
      gameView = gameBattleMusicView;
    } else if (game.getId().equals(Game.GAME_BIRD_RUSH)) {
      GameBirdRushView gameBirdRushView = new GameBirdRushView(getContext());
      subscriptionsGame.add(gameBirdRushView.onAddScore().subscribe(onAddScore));
      gameView = gameBirdRushView;
    } else if (game.isWeb()) {
      GameWebView gameWebView = new GameWebView(getContext());
      subscriptionsGame.add(gameWebView.onAddScore().subscribe(onAddScore));
      gameView = gameWebView;
    } else if (game.isCorona()) {
      GameCoronaView gameCoronaView = new GameCoronaView(getContext(), game);
      subscriptionsGame.add(gameCoronaView.onAddScore().subscribe(onAddScore));
      subscriptionsGame.add(gameCoronaView.onRevive().subscribe(onRevive));
      gameView = gameCoronaView;
    }

    gameView.setWebRTCRoom(webRTCRoom);
    game.initPeerMapObservable(onPeerMapChange);
    game.setDataList(mapGameData.get(game.getId()));
    gameView.start(game, masterMapObs, onPeerMapChange, onInvitedMapChange, onLiveViewsChange,
        userId);
    gameView.setNextGame();

    return gameView;
  }

  /**
   * PUBLIC
   */

  public void setWebRTCRoom(WebRTCRoom webRTCRoom) {
    this.webRTCRoom = webRTCRoom;
  }

  public void disposeGame() {
    subscriptionsGame.clear();
    if (currentGameView != null) {
      currentGameView.stop();
      removeView(currentGameView);
      currentGameView = null;
    }
  }

  public void dispose() {
    subscriptions.clear();
    invitedMap.clear();
    peerMap.clear();
    mapGameData.clear();
    onPeerMapChange = BehaviorSubject.create();
    onInvitedMapChange = BehaviorSubject.create();
    disposeGame();
  }

  /**
   * OBSERVABLES
   */

  public Observable<Game> onRestartGame() {
    return onRestartGame;
  }

  public Observable<Pair<String, Integer>> onAddScore() {
    return onAddScore;
  }

  public Observable<GameCoronaView> onRevive() {
    return onRevive;
  }

  public Observable<Game> onStopGame() {
    return onStopGame;
  }

  public Observable<Void> onPlayOtherGame() {
    return onPlayOtherGame;
  }
}
