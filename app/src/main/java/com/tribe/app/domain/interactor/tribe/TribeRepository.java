package com.tribe.app.domain.interactor.tribe;

/**
 * Created by tiago on 28/06/2016.
 */

import com.tribe.app.domain.entity.Tribe;

import rx.Observable;

/**
 * Interface that represents a Repository for getting {@link Tribe} related data.
 */
public interface TribeRepository {

    /**
     * Get an {@link Observable} which will emit a {@link Tribe} containing info about the tribe.
     *
     * @param
     */
    Observable<Tribe> sendTribe(final Tribe tribe);
}
