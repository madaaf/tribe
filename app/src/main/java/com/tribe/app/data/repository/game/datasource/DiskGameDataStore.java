package com.tribe.app.data.repository.game.datasource;

import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.realm.GameRealm;
import java.util.List;
import rx.Observable;

public class DiskGameDataStore implements GameDataStore {

  private final GameCache gameCache;

  public DiskGameDataStore(GameCache gameCache) {
    this.gameCache = gameCache;
  }

  @Override public Observable<Void> synchronizeGamesData() {
    return null;
  }

  @Override public Observable<List<GameRealm>> getGames() {
    return Observable.just(gameCache.getGames());
  }
}
