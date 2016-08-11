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
     * about the tribes received and sent.
     */
    Observable<List<TribeMessage>> tribes();

    /**
     * Get an {@link Observable} which will emit a {@link List< TribeMessage >} containing infos
     * about the tribes pending.
     */
    Observable<List<TribeMessage>> tribesPending();

    /**
     * Get an {@link Observable} which will emit a {@link TribeMessage} containing info about the tribe.
     *
     * @param tribeList the TribeMessage List to put as seen
     */
    Observable<List<TribeMessage>> markTribeListAsRead(final List<TribeMessage> tribeList);
}
