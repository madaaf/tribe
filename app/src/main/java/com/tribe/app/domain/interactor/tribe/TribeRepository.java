package com.tribe.app.domain.interactor.tribe;

/**
 * Created by tiago on 28/06/2016.
 */

import com.tribe.app.domain.entity.Friendship;
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
     * Get an {@link Observable} which will emit a {@link List<Friendship>} containing infos
     * about the tribes received and sent categorized by friendship.
     */
    Observable<List<Friendship>> tribes();
}
