package com.tribe.app.data.repository.tribe.datasource;

import com.tribe.app.data.realm.TribeRealm;

import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface TribeDataStore {

    /**
     * Get an {@link Observable} which will emit a {@link TribeRealm} containing info about the tribe.
     *
     * @param tribeRealm The tribe to save.
     */
    Observable<TribeRealm> saveTribe(TribeRealm tribeRealm);
}
