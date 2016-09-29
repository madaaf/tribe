package com.tribe.app.domain.interactor.tribe;

/**
 * Created by tiago on 28/06/2016.
 */

import com.tribe.app.domain.entity.TribeMessage;

import java.util.List;

import rx.Observable;

/**
 * Interface that represents a Repository for getting {@link TribeMessage} related data.
 */
public interface TribeRepository {

    /**
     * Get an {@link Observable} which will emit a {@link TribeMessage} containing info about the tribe.
     *
     * @param tribe the TribeMessage to save
     */
    Observable<TribeMessage> sendTribe(final TribeMessage tribe);

    /**
     * Get an {@link Observable} which will delete a tribe.
     *
     * @param tribe the TribeMessage to delete
     */
    Observable<Void> deleteTribe(final TribeMessage tribe);

    /**
     * Get an {@link Observable} which will emit a {@link List< TribeMessage >} containing infos
     * about the tribes not seen and sent.
     *
     * @param recipientId (friendship or group)
     */
    Observable<List<TribeMessage>> tribesNotSeen(String recipientId);

    /**
     * Get an {@link Observable} which will emit a {@link List< TribeMessage >} containing infos
     * about the tribes received.
     *
     * @param recipientId (friendship or group)
     */
    Observable<List<TribeMessage>> tribesReceived(String recipientId);

    /**
     * Get an {@link Observable} which will emit a {@link List< TribeMessage >} containing infos
     * about the tribes received and not seen.
     *
     * @param recipientId (friendship or group)
     */
    Observable<List<TribeMessage>> tribesForARecipient(String recipientId);

    /**
     * Get an {@link Observable} which will emit a {@link List< TribeMessage >} containing infos
     * about the tribes pending.
     */
    Observable<List<TribeMessage>> tribesPending();

    /**
     * Get an {@link Observable} which will emit a {@link List< TribeMessage >} containing info about the tribe.
     *
     * @param tribeList the TribeMessage List to put as seen
     */
    Observable<List<TribeMessage>> markTribeListAsRead(final List<TribeMessage> tribeList);

    /**
     * Get an {@link Observable} which will emit a {@link TribeMessage } containing info about the tribe saved.
     *
     * @param tribe the TribeMessage to put as saved
     */
    Observable<Void> markTribeAsSave(final TribeMessage tribe);
}
