package com.tribe.app.data.repository.user.datasource;

import com.tribe.app.data.cache.FriendshipCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.repository.friendship.datasource.FriendshipDataStore;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

/**
 * {@link FriendshipDataStore} implementation based on connections to the api (Cloud).
 */
public class CloudUserDataStore implements UserDataStore {

    private final TribeApi tribeApi;
    private final UserCache userCache;

    /**
     * Construct a {@link UserDataStore} based on connections to the api (Cloud).
     * @param userCache A {@link UserCache} to cache data retrieved from the api.
     * @param tribeApi an implementation of the api
     */
    public CloudUserDataStore(UserCache userCache, TribeApi tribeApi) {
        this.userCache = userCache;
        this.tribeApi = tribeApi;
    }

    @Override
    public Observable<UserRealm> loginWithUsername(String username, String password) {
        return this.tribeApi.loginWithUsername(new LoginEntity(username, password, "password"));
    }
}
