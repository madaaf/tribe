package com.tribe.tribelivesdk.game;

import android.content.Context;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

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

  @Inject public GameManager(Context context) {
    gameList = new ArrayList<>();
  }

  public void addGame(Game game) {
    gameList.add(game);
  }

  public void initSubscriptions() {

  }

  public void initFrameSizeChangeObs(Observable<Frame> obs) {
    subscriptions.add(obs.subscribe(frame -> {
      for (Game game : gameList) {
        game.onFrameSizeChange(frame);
      }
    }));
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

    if (currentGame != null && currentGame instanceof GamePostIt) {
      GamePostIt gamePostIt = (GamePostIt) game;
      gamePostIt.generateNewName();
    }
  }

  public void stopCapture() {
    subscriptions.clear();
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
}
