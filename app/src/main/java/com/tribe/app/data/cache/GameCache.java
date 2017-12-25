package com.tribe.app.data.cache;

import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.domain.entity.trivia.TriviaQuestions;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton public interface GameCache {

  void putGames(List<GameRealm> gameRealmList);

  List<GameRealm> getGames();

  void updateLeaderboard(String gameId, boolean friendsOnly, List<ScoreRealm> scoreRealmList);

  void updateLeaderboard(String userId, List<ScoreRealm> scoreRealmList);

  List<ScoreRealm> getGameLeaderboard(String gameId, boolean friendsOnly);

  List<ScoreRealm> getUserLeaderboard(String userId);

  void setTriviaData(Map<String, List<TriviaQuestions>> mapTrivia);

  Map<String, List<TriviaQuestions>> getTriviaData();
}
