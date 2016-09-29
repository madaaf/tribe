package com.tribe.app.data.repository.tribe.datasource;

import android.support.v4.util.Pair;

import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Map<String, List<Pair<String, Object>>> tribeUpdates = new HashMap<>();

        for (TribeRealm tribeRealm : tribeRealmList) {
            List<Pair<String, Object>> values = new ArrayList<>();
            values.add(Pair.create(TribeRealm.MESSAGE_RECEIVING_STATUS, MessageReceivingStatus.STATUS_SEEN));
            tribeUpdates.put(tribeRealm.getLocalId(), values);
        }

        tribeCache.update(tribeUpdates);

        return Observable.just(tribeRealmList);
    }

    @Override
    public Observable<Void> markTribeAsSave(TribeRealm tribeRealm) {
        return null;
    }
}
