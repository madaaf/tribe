package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.TribeI420Frame;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 23/05/2017.
 */

@Singleton public class GameManager {

  private static GameManager instance;

  public static GameManager getInstance(Context context) {
    if (instance == null) {
      instance = new GameManager(context);
    }

    return instance;
  }

  // VARIABLES
  private List<Game> gameList;
  private Game currentGame = null;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Frame> onRemoteFrame = PublishSubject.create();
  private PublishSubject<TribeI420Frame> onLocalFrame = PublishSubject.create();

  @Inject public GameManager(Context context) {
    gameList = new ArrayList<>();
  }

  public void addGame(Game game) {
    gameList.add(game);
  }

  public void initSubscriptions() {
    for (Game game : gameList) {
      subscriptions.add(game.onLocalFrame().subscribe(onLocalFrame));
      subscriptions.add(game.onRemoteFrame().subscribe(onRemoteFrame));
    }
  }

  public void initFrameSizeChangeObs(Observable<Frame> obs) {
    subscriptions.add(obs.subscribe(frame -> {
      for (Game game : gameList) {
        game.onFrameSizeChange(frame);
      }
    }));
  }

  public void initOnNewFrameObs(Observable<Frame> obs) {
    subscriptions.add(obs.subscribe(frame -> currentGame.apply(frame)));
  }

  public List<Game> getGames() {
    return gameList;
  }

  public Game getGameById(String id) {
    for (Game game : gameList) {
      if (game.getId().equals(id)) return game;
    }

    return null;
  }

  public void setCurrentGame(Game game) {
    this.currentGame = game;
    if (currentGame != null) {
      Timber.e("SOEF Set current game  : " + currentGame.getId());
      if (currentGame instanceof GamePostIt) {
        GamePostIt gamePostIt = (GamePostIt) game;
        gamePostIt.generateNewName();
      }

      if (currentGame instanceof GameDraw) {
        GameDraw gameDraw = (GameDraw) game;
        gameDraw.generateNewDatas();
      }
      if (currentGame instanceof GameChallenge) {
        GameChallenge gameChallenge = (GameChallenge) game;
        gameChallenge.generateNewDatas();
      }
    }
  }

  public void stop() {
    this.currentGame = null;
  }

  public Game getCurrentGame() {
    return currentGame;
  }

  public boolean isFacialRecognitionNeeded() {
    return currentGame != null && currentGame.getId().equals(Game.GAME_POST_IT);
  }

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
    for (Game game : gameList) {
      game.dispose();
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Frame> onRemoteFrame() {
    return onRemoteFrame;
  }

  public Observable<TribeI420Frame> onLocalFrame() {
    return onLocalFrame;
  }

  public void setCurrentDataGame(String name, TribeGuest currentPlayer) {
    if (currentGame.getId().equals(Game.GAME_DRAW)) {
      Timber.e("soef set current data game " + name + " " + currentPlayer.getId());
      ((GameDraw) currentGame).setCurrentDrawer(currentPlayer);
      ((GameDraw) currentGame).setCurrentDrawName(name);
    } else if (currentGame.getId().equals(Game.GAME_CHALLENGE)) {
      Timber.e("soef set current data game " + name + " " + currentPlayer.getId());
      ((GameChallenge) currentGame).setCurrentChallenger(currentPlayer);
      ((GameChallenge) currentGame).setCurrentChallenge(name);
    }
  }
}
