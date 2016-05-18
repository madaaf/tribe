package com.tribe.app.domain.interactor.friendship;

/**
 * Created by tiago on 04/05/2016.
 */


import com.tribe.app.domain.entity.Friendship;

import java.util.List;

import rx.Observable;

/**
 * Interface that represents a Repository for getting {@link Friendship} related data.
 */
public interface FriendshipRepository {

    /**
     * Get an {@link Observable} which will emit a {@link Friendship}.
     *
     * @param userId The userId of the user we want to get the friendships of.
     */
    Observable<List<Friendship>> friendships(final int userId);
}
