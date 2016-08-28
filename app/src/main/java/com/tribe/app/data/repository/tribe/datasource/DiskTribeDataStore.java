package com.tribe.app.data.repository.tribe.datasource;

import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
    public Observable<Void> deleteTribe(TribeRealm tribeRealm) {
        return tribeCache.delete(tribeRealm);
    }

    @Override
    public Observable<TribeRealm> sendTribe(TribeRealm tribeRealm) {
        return tribeCache.put(tribeRealm);
    }

    @Override
    public Observable<List<TribeRealm>> tribesNotSeen(String friendshipId) {
        return tribeCache.tribesNotSeen(friendshipId).debounce(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public Observable<List<TribeRealm>> tribesReceived(String friendshipId) {
        return tribeCache.tribesReceived(friendshipId).debounce(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public Observable<List<TribeRealm>> tribesPending() {
        return tribeCache.tribesPending().debounce(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public Observable<List<TribeRealm>> markTribeListAsRead(List<TribeRealm> tribeRealmList) {
        for (TribeRealm tribeRealm : tribeRealmList) {
            tribeRealm.setMessageReceivingStatus(MessageReceivingStatus.STATUS_SEEN);
        }

        tribeCache.put(tribeRealmList);

        return Observable.just(tribeRealmList);
    }
}
