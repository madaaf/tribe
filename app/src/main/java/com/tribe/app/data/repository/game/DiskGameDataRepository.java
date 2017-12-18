package com.tribe.app.data.repository.game;

import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.data.realm.mapper.GameRealmDataMapper;
import com.tribe.app.data.realm.mapper.ScoreRealmDataMapper;
import com.tribe.app.data.repository.game.datasource.GameDataStore;
import com.tribe.app.data.repository.game.datasource.GameDataStoreFactory;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.interactor.game.GameRepository;
import com.tribe.tribelivesdk.game.Game;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class DiskGameDataRepository implements GameRepository {

  private final GameDataStoreFactory dataStoreFactory;
  private final GameRealmDataMapper gameRealmDataMapper;
  private final ScoreRealmDataMapper scoreRealmDataMapper;

  @Inject public DiskGameDataRepository(GameDataStoreFactory dataStoreFactory,
      GameRealmDataMapper gameRealmDataMapper, ScoreRealmDataMapper scoreRealmDataMapper) {
    this.dataStoreFactory = dataStoreFactory;
    this.gameRealmDataMapper = gameRealmDataMapper;
    this.scoreRealmDataMapper = scoreRealmDataMapper;
  }

  @Override public Observable<Void> synchronizeGamesData(String lang) {
    return null;
  }

  @Override public Observable<List<Game>> getGames() {
    GameDataStore gameDataStore = dataStoreFactory.createDiskDataStore();
    return gameDataStore.getGames().map(gameRealm -> gameRealmDataMapper.transform(gameRealm));
  }

  @Override public Observable<List<Score>> getGameLeaderBoard(String gameId, boolean friendsOnly,
      int limit, int offset) {
    GameDataStore gameDataStore = dataStoreFactory.createDiskDataStore();
    return gameDataStore.getGameLeaderBoard(gameId, friendsOnly, limit, offset)
        .map(scoreRealmList -> scoreRealmDataMapper.transform(scoreRealmList));
  }

  @Override public Observable<List<Score>> getUserLeaderboard(String userId) {
    GameDataStore gameDataStore = dataStoreFactory.createDiskDataStore();
    return gameDataStore.getUserLeaderboard(userId)
        .map(scoreRealmList -> scoreRealmDataMapper.transform(scoreRealmList));
  }

  @Override public Observable<Void> addScore(String gameId, Integer score) {
    return null;
  }

  @Override public Observable<List<Score>> getFriendsScores(String gameId) {
    GameDataStore gameDataStore = dataStoreFactory.createDiskDataStore();
    return gameDataStore.getFriendsScore(gameId)
        .map(scoreRealmList -> scoreRealmDataMapper.transform(scoreRealmList));
  }
}
