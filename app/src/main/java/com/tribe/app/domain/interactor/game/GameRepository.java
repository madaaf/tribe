package com.tribe.app.domain.interactor.game;

/**
 * Created by tiago on 04/05/2016.
 */

import com.tribe.app.data.network.entity.AddScoreEntity;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.GameFile;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.tribelivesdk.game.Game;
import java.util.List;
import java.util.Map;
import rx.Observable;

public interface GameRepository {

  Observable<Void> synchronizeGamesData(String lang);

  Observable<List<String>> synchronizeGameData(String lang, String gameId);

  Observable<List<Game>> getGames();

  Observable<List<Score>> getGameLeaderBoard(String gameId, List<Contact> usersId);

  Observable<List<Score>> getUserLeaderboard(String userId);

  Observable<AddScoreEntity> addScore(String gameId, Integer score);

  Observable<List<Score>> getFriendsScores(String gameId);

  Observable<Map<String, List<TriviaQuestion>>> getTriviaData();

  Observable<Map<String, BattleMusicPlaylist>> getBattleMusicData();

  Observable<GameFile> getGameFile(String url);

  Observable<Score> getUserBestScore(String gameId);
}
