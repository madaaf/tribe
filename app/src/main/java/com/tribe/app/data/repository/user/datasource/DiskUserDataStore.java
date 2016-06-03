package com.tribe.app.data.repository.user.datasource;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.UserRealm;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

/**
 * {@link UserDataStore} implementation based on connections to the database.
 */
public class DiskUserDataStore implements UserDataStore {

    private final UserCache userCache;
    /**
     * Construct a {@link UserDataStore} based on the database.
     * @param userCache A {@link UserCache} to retrieve the data.
     */
    public DiskUserDataStore(UserCache userCache) {
        this.userCache = userCache;
    }

    @Override
    public Observable<AccessToken> loginWithUsername(String username, String password) { return null; }

    @Override
    public Observable<UserRealm> userInfos(String userId) {
        return this.userCache.userInfos(userId);
    }
}
