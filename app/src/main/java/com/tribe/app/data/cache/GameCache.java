package com.tribe.app.data.cache;

import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import java.util.List;
import javax.inject.Singleton;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton public interface GameCache {

  void putGames(List<GameRealm> gameRealmList);

  List<GameRealm> getGames();

  void updateLeaderboard(String gameId, boolean friendsOnly, List<ScoreRealm> scoreRealmList);

  List<ScoreRealm> getLeaderboard(String gameId, boolean friendsOnly);
}
