package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.FriendshipRealm;

import javax.inject.Inject;

/**
 * Created by tiago on 06/05/2016.
 */
public class FriendshipCacheImpl implements FriendshipCache {

    private Context context;

    @Inject
    public FriendshipCacheImpl(Context context) {
        this.context = context;
    }

    public boolean isExpired() {
        return true;
    }

    public boolean isCached(int friendshipId) {
        return false;
    }

    public void put(FriendshipRealm friendshipRealm) {

    }
}
