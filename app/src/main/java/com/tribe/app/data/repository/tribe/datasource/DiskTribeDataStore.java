package com.tribe.app.data.repository.tribe.datasource;

import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.realm.TribeRealm;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * {@link TribeDataStoreFactory} implementation based on connections to the database.
 */
public class DiskTribeDataStore implements TribeDataStore {

    private final TribeCache tribeCache;
    private final UserCache userCache;

    /**
     * Construct a {@link TribeDataStore} based on the database.
     * @param tribeCache A {@link TribeCache} to retrieve the data.
     */
    public DiskTribeDataStore(TribeCache tribeCache, UserCache userCache) {
        this.tribeCache = tribeCache;
        this.userCache = userCache;
    }

    @Override
    public Observable<Void> deleteTribe(TribeRealm tribeRealm) {
        return tribeCache.delete(tribeRealm);
    }

    @Override
    public Observable<TribeRealm> sendTribe(TribeRealm tribeRealm) {
        tribeRealm.setFrom(userCache.userInfosNoObs(tribeRealm.getFrom().getId()));
        return tribeCache.insert(tribeRealm);
    }

    @Override
    public Observable<List<TribeRealm>> tribesNotSeen(String recipientId) {
        return tribeCache.tribesNotSeen(recipientId).debounce(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public Observable<List<TribeRealm>> tribesReceived(String recipientId) {
        return tribeCache.tribesReceived(recipientId).debounce(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public Observable<List<TribeRealm>> tribesForARecipient(String recipientId) {
        return tribeCache.tribesForARecipient(recipientId).debounce(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public Observable<List<TribeRealm>> tribesPending() {
        return tribeCache.tribesPending().debounce(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public Observable<List<TribeRealm>> markTribeListAsRead(List<TribeRealm> tribeRealmList) {
        return null;
    }

    @Override
    public Observable<Void> markTribeAsSave(TribeRealm tribeRealm) {
        return null;
    }
}
