package com.tribe.app.data.repository.tribe.datasource;

import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.TribeRealm;

import rx.Observable;

/**
 * {@link TribeDataStoreFactory} implementation based on connections to the database.
 */
public class DiskTribeDataStore implements TribeDataStore {

    private final TribeCache tribeCache;
    /**
     * Construct a {@link TribeDataStore} based on the database.
     * @param tribeCache A {@link TribeCache} to retrieve the data.
     */
    public DiskTribeDataStore(TribeCache tribeCache) {
        this.tribeCache = tribeCache;
    }

    @Override
    public Observable<TribeRealm> saveTribe(TribeRealm tribeRealm) {
        return tribeCache.put(tribeRealm);
    }

    @Override
    public Observable<Void> deleteTribe(TribeRealm tribeRealm) {
        return tribeCache.delete(tribeRealm);
    }

    @Override
    public Observable<TribeRealm> sendTribe(TribeRealm tribeRealm) {
        return null;
    }
}
