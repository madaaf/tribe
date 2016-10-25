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
     * about the tribes not seen and sent.
     *
     * @param recipientId (friendship or group)
     */
    Observable<List<TribeRealm>> tribesNotSeen(String recipientId);

    /**
     * Get an {@link Observable} which will emit a {@link List < TribeMessage >} containing infos
     * about the tribes received.
     *
     * @param recipientId (friendship or group)
     */
    Observable<List<TribeRealm>> tribesReceived(String recipientId);


    /**
     * Get an {@link Observable} which will emit a {@link List < TribeMessage >} containing infos
     * about the tribes received.
     *
     * @param recipientId (friendship or group)
     */
    Observable<List<TribeRealm>> tribesForARecipient(String recipientId);

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

    /**
     * Get an {@link Observable} which will emit a {@link TribeRealm } containing info about the tribe saved.
     *
     * @param tribeRealm the TribeRealm to put as saved
     */
    Observable<Void> markTribeAsSave(final TribeRealm tribeRealm);

    Observable<Void> confirmTribe(String tribeId);
}
