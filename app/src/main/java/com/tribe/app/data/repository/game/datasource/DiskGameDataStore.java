package com.tribe.app.data.repository.game.datasource;

import android.util.Pair;
import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.network.entity.AddScoreEntity;
import com.tribe.app.data.realm.GameFileRealm;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import rx.Observable;

public class DiskGameDataStore implements GameDataStore {

  private final GameCache gameCache;

  public DiskGameDataStore(GameCache gameCache) {
    this.gameCache = gameCache;
  }

  @Override public Observable<Void> synchronizeGamesData() {
    return null;
  }

  @Override public Observable<List<String>> synchronizeGameData(String gameId) {
    return null;
  }

  @Override public Observable<List<GameRealm>> getGames() {
    return Observable.just(gameCache.getGames());
  }

  @Override public Observable<List<ScoreRealm>> getGameLeaderBoard(String gameId) {
    return Observable.just(gameCache.getGameLeaderboard(gameId));
  }

  @Override public Observable<List<ScoreRealm>> getUserLeaderboard(String userId) {
    return Observable.just(gameCache.getUserLeaderboard(userId));
  }

  @Override public Observable<AddScoreEntity> addScore(String gameId, Integer score) {
    return null;
  }

  @Override public Observable<List<ScoreRealm>> getFriendsScore(String gameId) {
    return Observable.just(gameCache.getGameLeaderboard(gameId));
  }

  @Override public Observable<Map<String, List<TriviaQuestion>>> getTriviaData() {
    return Observable.just(gameCache.getTriviaData());
  }

  @Override public Observable<Map<String, BattleMusicPlaylist>> getBattleMusicData() {
    return Observable.just(gameCache.getBattleMusicData());
  }

  @Override public Observable<GameFileRealm> getGameFile(String url) {
    GameFileRealm gameFileRealm = gameCache.getGameFile(url);

    if (gameFileRealm != null) {
      if (!gameFileRealm.getDownloadStatus().equals(GameFileRealm.STATUS_DOWNLOADED)) {
        List<Pair<String, Object>> updates = new ArrayList<>();
        updates.add(Pair.create(GameFileRealm.DOWNLOAD_STATUS, GameFileRealm.STATUS_TO_DOWNLOAD));
        gameCache.updateGameFiles(url, updates);
      }

      return gameCache.getGameFileObs(url);
    }

    return null;
  }
}
