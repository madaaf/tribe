package com.tribe.app.data.cache;

import com.tribe.app.data.realm.FriendshipRealm;

import javax.inject.Singleton;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton
public interface FriendshipCache {

    public boolean isExpired();
    public boolean isCached(int friendshipId);
    public void put(FriendshipRealm friendshipRealm);
}
