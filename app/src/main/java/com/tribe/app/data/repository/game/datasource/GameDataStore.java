package com.tribe.app.data.repository.game.datasource;

import com.tribe.app.data.network.entity.AddScoreEntity;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import java.util.List;
import java.util.Map;
import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface GameDataStore {

  Observable<Void> synchronizeGamesData();

  Observable<List<GameRealm>> getGames();

  Observable<List<ScoreRealm>> getGameLeaderBoard(String gameId, boolean friendsOnly, int limit,
      int offset);

  Observable<List<ScoreRealm>> getUserLeaderboard(String userId);

  Observable<AddScoreEntity> addScore(String gameId, Integer score);

  Observable<List<ScoreRealm>> getFriendsScore(String gameId);

  Observable<Map<String, List<TriviaQuestion>>> getTriviaData();
}
