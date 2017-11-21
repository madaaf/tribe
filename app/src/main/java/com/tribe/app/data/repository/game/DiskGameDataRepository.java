package com.tribe.app.data.repository.game;

import com.tribe.app.data.realm.mapper.GameRealmDataMapper;
import com.tribe.app.data.repository.game.datasource.GameDataStore;
import com.tribe.app.data.repository.game.datasource.GameDataStoreFactory;
import com.tribe.app.domain.interactor.game.GameRepository;
import com.tribe.tribelivesdk.game.Game;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class DiskGameDataRepository implements GameRepository {

  private final GameDataStoreFactory dataStoreFactory;
  private final GameRealmDataMapper gameRealmDataMapper;

  @Inject public DiskGameDataRepository(GameDataStoreFactory dataStoreFactory, GameRealmDataMapper gameRealmDataMapper) {
    this.dataStoreFactory = dataStoreFactory;
    this.gameRealmDataMapper = gameRealmDataMapper;
  }

  @Override public Observable<Void> synchronizeGamesData(String lang) {
    return null;
  }

  @Override public Observable<List<Game>> getGames() {
    GameDataStore gameDataStore = dataStoreFactory.createDiskDataStore();
    return gameDataStore.getGames().map(gameRealm -> gameRealmDataMapper.transform(gameRealm));
  }
}
