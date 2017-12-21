package com.tribe.app.data.repository.game.datasource;

import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.network.entity.AddScoreEntity;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.domain.entity.trivia.TriviaQuestions;
import java.util.List;
import rx.Observable;

public class DiskGameDataStore implements GameDataStore {

  private final GameCache gameCache;

  public DiskGameDataStore(GameCache gameCache) {
    this.gameCache = gameCache;
  }

  @Override public Observable<Void> synchronizeGamesData() {
    return null;
  }

  @Override public Observable<List<GameRealm>> getGames() {
    return Observable.just(gameCache.getGames());
  }

  @Override
  public Observable<List<ScoreRealm>> getGameLeaderBoard(String gameId, boolean friendsOnly,
      int limit, int offset) {
    return Observable.just(gameCache.getGameLeaderboard(gameId, friendsOnly));
  }

  @Override public Observable<List<ScoreRealm>> getUserLeaderboard(String userId) {
    return Observable.just(gameCache.getUserLeaderboard(userId));
  }

  @Override public Observable<AddScoreEntity> addScore(String gameId, Integer score) {
    return null;
  }

  @Override public Observable<List<ScoreRealm>> getFriendsScore(String gameId) {
    return Observable.just(gameCache.getGameLeaderboard(gameId, true));
  }

  @Override public Observable<List<TriviaQuestions>> getTriviaData() {
    return null;
  }
}
