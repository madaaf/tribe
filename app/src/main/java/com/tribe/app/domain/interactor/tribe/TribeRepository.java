package com.tribe.app.domain.interactor.tribe;

/**
 * Created by tiago on 28/06/2016.
 */

import com.tribe.app.domain.entity.Tribe;

import java.util.List;

import rx.Observable;

/**
 * Interface that represents a Repository for getting {@link Tribe} related data.
 */
public interface TribeRepository {

    /**
     * Get an {@link Observable} which will emit a {@link Tribe} containing info about the tribe.
     *
     * @param tribe the Tribe to save
     */
    Observable<Tribe> sendTribe(final Tribe tribe);

    /**
     * Get an {@link Observable} which will delete a tribe.
     *
     * @param tribe the Tribe to delete
     */
    Observable<Void> deleteTribe(final Tribe tribe);

    /**
     * Get an {@link Observable} which will emit a {@link List<Tribe>} containing infos
     * about the tribes received and sent.
     */
    Observable<List<Tribe>> tribes();

    /**
     * Get an {@link Observable} which will emit a {@link List<Tribe>} containing infos
     * about the tribes pending.
     */
    Observable<List<Tribe>> tribesPending();

    /**
     * Get an {@link Observable} which will emit a {@link Tribe} containing info about the tribe.
     *
     * @param tribeList the Tribe List to put as seen
     */
    Observable<List<Tribe>> markTribeListAsRead(final List<Tribe> tribeList);
}
