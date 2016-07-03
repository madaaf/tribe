package com.tribe.app.data.repository.tribe.datasource;

import android.content.Context;

import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory that creates different implementations of {@link TribeDataStoreFactory}.
 */
@Singleton
public class TribeDataStoreFactory {

    private final Context context;
    private final TribeCache tribeCache;
    private final TribeApi tribeApi;
    private final AccessToken accessToken;

    @Inject
    public TribeDataStoreFactory(Context context, TribeCache tribeCache, TribeApi tribeApi, AccessToken accessToken) {
        if (context == null || tribeCache == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!");
        }

        this.context = context.getApplicationContext();
        this.tribeCache = tribeCache;
        this.tribeApi = tribeApi;
        this.accessToken = accessToken;
    }

    /**
     * Create {@link TribeDataStore}
     */
    public TribeDataStore createDiskDataStore() { return new DiskTribeDataStore(tribeCache); }

    /**
     * Create {@link TribeDataStore} to retrieve data from the Cloud.
     */
    public TribeDataStore createCloudDataStore() {
        return new CloudTribeDataStore(this.tribeCache, this.tribeApi, this.accessToken, this.context);
    }
}
