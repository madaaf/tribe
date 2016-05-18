package com.tribe.app.data.repository.friendship.datasource;

import com.tribe.app.data.cache.FriendshipCache;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.network.TribeApi;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;

/**
 * {@link FriendshipDataStore} implementation based on connections to the api (Cloud).
 */
public class CloudFriendshipDataStore implements FriendshipDataStore {

    private final TribeApi tribeApi;
    private final FriendshipCache friendshipCache;

    private final Action1<FriendshipRealm> saveToCacheAction = friendshipRealm -> {
        if (friendshipRealm != null) {
            CloudFriendshipDataStore.this.friendshipCache.put(friendshipRealm);
        }
    };

    /**
     * Construct a {@link FriendshipDataStore} based on connections to the api (Cloud).
     * @param friendshipCache A {@link FriendshipCache} to cache data retrieved from the api.
     * @param tribeApi an implementation of the api
     */
    public CloudFriendshipDataStore(FriendshipCache friendshipCache, TribeApi tribeApi) {
        this.friendshipCache = friendshipCache;
        this.tribeApi = tribeApi;
    }

    @Override
    public Observable<List<FriendshipRealm>> friendships(int userId) {
        return this.tribeApi.friendships(userId);
    }
}
