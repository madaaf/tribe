package com.tribe.app.data.repository.user.datasource;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.data.cache.FriendshipCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.repository.friendship.datasource.FriendshipDataStore;
import com.tribe.app.domain.entity.User;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

/**
 * {@link FriendshipDataStore} implementation based on connections to the api (Cloud).
 */
public class CloudUserDataStore implements UserDataStore {

    private final TribeApi tribeApi;
    private final UserCache userCache;
    private final Context context;

    /**
     * Construct a {@link UserDataStore} based on connections to the api (Cloud).
     * @param userCache A {@link UserCache} to cache data retrieved from the api.
     * @param tribeApi an implementation of the api
     * @param context the context
     */
    public CloudUserDataStore(UserCache userCache, TribeApi tribeApi, Context context) {
        this.userCache = userCache;
        this.tribeApi = tribeApi;
        this.context = context;
    }

    @Override
    public Observable<UserRealm> loginWithUsername(String username, String password) {
        return this.tribeApi.loginWithUsername(new LoginEntity(username, password, "password"));
    }

    @Override
    public Observable<UserRealm> getUserInfos(String userId) {
        return this.tribeApi.getUserInfos(context.getString(R.string.user_infos));
    }
}
