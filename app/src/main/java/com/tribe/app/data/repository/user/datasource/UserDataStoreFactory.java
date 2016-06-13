package com.tribe.app.data.repository.user.datasource;

import android.content.Context;

import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.LoginApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory that creates different implementations of {@link UserDataStoreFactory}.
 */
@Singleton
public class UserDataStoreFactory {

    private final Context context;
    private final UserCache userCache;
    private final TribeApi tribeApi;
    private final LoginApi loginApi;
    private final AccessToken accessToken;

    @Inject
    public UserDataStoreFactory(Context context, UserCache userCache, TribeApi tribeApi, LoginApi loginApi, AccessToken accessToken) {
        if (context == null || userCache == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!");
        }

        this.context = context.getApplicationContext();
        this.userCache = userCache;
        this.tribeApi = tribeApi;
        this.loginApi = loginApi;
        this.accessToken = accessToken;
    }

    /**
     * Create {@link UserDataStore}
     */
    public UserDataStore createDiskDataStore() { return new DiskUserDataStore(userCache); }

    /**
     * Create {@link UserDataStore} to retrieve data from the Cloud.
     */
    public UserDataStore createCloudDataStore() {
        return new CloudUserDataStore(this.userCache, this.tribeApi, loginApi, this.accessToken, this.context);
    }
}
