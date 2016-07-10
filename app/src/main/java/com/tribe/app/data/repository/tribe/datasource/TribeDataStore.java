package com.tribe.app.data.repository.tribe.datasource;

import com.tribe.app.data.realm.TribeRealm;

import java.util.List;

import io.realm.RealmList;
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
     * Get an {@link Observable} which will emit a {@link List < Tribe >} containing infos
     * about the tribes received and sent.
     */
    Observable<RealmList<TribeRealm>> tribes();
}