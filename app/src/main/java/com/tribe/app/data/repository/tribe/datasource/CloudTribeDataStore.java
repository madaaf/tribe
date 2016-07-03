package com.tribe.app.data.repository.tribe.datasource;

import android.content.Context;

import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.TribeRealm;

import rx.Observable;

/**
 * {@link TribeDataStore} implementation based on connections to the api (Cloud).
 */
public class CloudTribeDataStore implements TribeDataStore {

    private final TribeApi tribeApi;
    private final TribeCache tribeCache;
    private final Context context;
    private final AccessToken accessToken;

    /**
     * Construct a {@link TribeDataStore} based on connections to the api (Cloud).
     * @param tribeCache A {@link UserCache} to cache data retrieved from the api.
     * @param tribeApi an implementation of the api
     * @param context the context
     * @param accessToken the access token
     */
    public CloudTribeDataStore(TribeCache tribeCache, TribeApi tribeApi, AccessToken accessToken, Context context) {
        this.tribeCache = tribeCache;
        this.tribeApi = tribeApi;
        this.context = context;
        this.accessToken = accessToken;
    }

    @Override
    public Observable<TribeRealm> saveTribe(TribeRealm tribeRealm) {
        return null;
    }
}
