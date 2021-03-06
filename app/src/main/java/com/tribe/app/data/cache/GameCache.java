package com.tribe.app.data.cache;

import android.util.Pair;
import com.tribe.app.data.realm.GameFileRealm;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton public interface GameCache {

  void putGames(List<GameRealm> gameRealmList);

  List<GameRealm> getGames();

  void updateLeaderboard(String gameId, boolean friendsOnly, List<ScoreRealm> scoreRealmList);

  void updateLeaderboard(String userId, List<ScoreRealm> scoreRealmList);

  List<ScoreRealm> getGameLeaderboard(String gameId);

  List<ScoreRealm> getUserLeaderboard(String userId);

  void setTriviaData(Map<String, List<TriviaQuestion>> mapTrivia);

  Map<String, List<TriviaQuestion>> getTriviaData();

  void setBattleMusicData(Map<String, BattleMusicPlaylist> mapBattleMusic);

  Map<String, BattleMusicPlaylist> getBattleMusicData();

  Observable<List<GameFileRealm>> getFilesToDownload();

  GameFileRealm getGameFile(String url);

  Observable<GameFileRealm> getGameFileObs(String url);

  void updateGameFiles(Map<String, List<Pair<String, Object>>> updateList);

  void updateGameFiles(String url, List<Pair<String, Object>> updateList);
}
