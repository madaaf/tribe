package com.tribe.app.data.repository.game.datasource;

import com.tribe.app.data.network.entity.AddScoreEntity;
import com.tribe.app.data.realm.GameFileRealm;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import java.util.List;
import java.util.Map;
import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface GameDataStore {

  Observable<Void> synchronizeGamesData();

  Observable<List<String>> synchronizeGameData(String gameId);

  Observable<List<GameRealm>> getGames();

  Observable<List<ScoreRealm>> getGameLeaderBoard(String gameId, List<Contact> usersId);

  Observable<List<ScoreRealm>> getUserLeaderboard(String userId);

  Observable<AddScoreEntity> addScore(String gameId, Integer score);

  Observable<List<ScoreRealm>> getFriendsScore(String gameId);

  Observable<Map<String, List<TriviaQuestion>>> getTriviaData();

  Observable<Map<String, BattleMusicPlaylist>> getBattleMusicData();

  Observable<GameFileRealm> getGameFile(String url);

  Observable<ScoreRealm> getUserBestScore(String gameId);
}
