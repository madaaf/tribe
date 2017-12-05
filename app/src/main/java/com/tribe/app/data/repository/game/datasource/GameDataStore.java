package com.tribe.app.data.repository.game.datasource;

import com.tribe.app.data.realm.GameRealm;
import java.util.List;
import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface GameDataStore {

  Observable<Void> synchronizeGamesData();

  Observable<List<GameRealm>> getGames();
}
