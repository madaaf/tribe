package com.tribe.app.data.repository.game.datasource;

import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import java.util.List;
import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface GameDataStore {

  Observable<Void> synchronizeGamesData();

  Observable<List<GameRealm>> getGames();

  Observable<List<ScoreRealm>> getGameLeaderBoard(String gameId, boolean friendsOnly, int offset);

  Observable<List<ScoreRealm>> getUserLeaderboard(String userId);

  Observable<Void> addScore(String gameId, Integer score);
}
