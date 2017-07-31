package com.tribe.app.data.repository.game;

import com.tribe.app.data.repository.game.datasource.GameDataStore;
import com.tribe.app.data.repository.game.datasource.GameDataStoreFactory;
import com.tribe.app.domain.interactor.game.GameRepository;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class CloudGameDataRepository implements GameRepository {

  private final GameDataStoreFactory dataStoreFactory;

  @Inject public CloudGameDataRepository(GameDataStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }

  @Override public Observable<List<String>> getNamesForPostItGame(String lang) {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.getNamesForPostItGame();
  }

  @Override public Observable<List<String>> getDataForChallengeGame(String lang) {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.getDataForChallengeGame();
  }

  @Override public Observable<List<String>> getNamesForDrawGame(String lang) {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.getNamesForDrawGame();
  }
}
