package com.tribe.app.data.repository.tribe.datasource;

import com.tribe.app.data.realm.TribeRealm;

import java.util.List;

import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface TribeDataStore {

    /**
     * Get an {@link Observable} which will delete a tribe.
     *
     * @param tribeRealm The tribe to delete.
     */
    Observable<Void> deleteTribe(TribeRealm tribeRealm);

    /**
     * Get an {@link Observable} which will emit a tribe.
     *
     * @param tribeRealm The tribe to send.
     */
    Observable<TribeRealm> sendTribe(TribeRealm tribeRealm);

    /**
     * Get an {@link Observable} which will emit a {@link List < TribeMessage >} containing infos
     * about the tribes received and sent.
     */
    Observable<List<TribeRealm>> tribes();

    /**
     * Get an {@link Observable} which will emit a {@link List < TribeMessage >} containing infos
     * about the tribes pending.
     */
    Observable<List<TribeRealm>> tribesPending();

    /**
     * Get an {@link Observable} which will emit a {@link List < TribeMessage >} containing infos
     * about the tribes pending.
     */
    Observable<List<TribeRealm>> markTribeListAsRead(List<TribeRealm> tribeRealmList);
}
