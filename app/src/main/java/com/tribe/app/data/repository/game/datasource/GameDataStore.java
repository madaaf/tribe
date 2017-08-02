package com.tribe.app.data.repository.game.datasource;

import java.util.List;
import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface GameDataStore {

  Observable<List<String>> getNamesForPostItGame();

  Observable<List<String>> getDataForChallengeGame();

  Observable<List<String>> getNamesForDrawGame();
}
