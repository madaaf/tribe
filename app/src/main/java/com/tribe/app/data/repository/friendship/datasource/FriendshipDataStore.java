package com.tribe.app.data.repository.friendship.datasource;

import com.tribe.app.data.realm.FriendshipRealm;

import java.util.List;

import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface FriendshipDataStore {
    /**
     * Get an {@link rx.Observable} which will emit a List of {@link FriendshipRealm}.
     * @param userId the id of the user for which we'll search the friendships
     */
    Observable<List<FriendshipRealm>> friendships(int userId);
}
