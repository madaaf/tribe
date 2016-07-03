package com.tribe.app.data.repository.tribe.datasource;

import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.domain.entity.Tribe;

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
}
