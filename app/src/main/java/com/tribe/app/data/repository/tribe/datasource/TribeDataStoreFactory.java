package com.tribe.app.data.repository.tribe.datasource;

import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;

import java.text.SimpleDateFormat;

import javax.inject.Inject;
import javax.inject.Named;
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
    private final SimpleDateFormat simpleDateFormat;

    @Inject
    public TribeDataStoreFactory(Context context, TribeCache tribeCache, TribeApi tribeApi, AccessToken accessToken,
                                 @Named("utcSimpleDate") SimpleDateFormat simpleDateFormat, JobManager jobManager) {
        if (context == null || tribeCache == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!");
        }

        this.context = context.getApplicationContext();
        this.tribeCache = tribeCache;
        this.tribeApi = tribeApi;
        this.accessToken = accessToken;
        this.simpleDateFormat = simpleDateFormat;
    }

    /**
     * Create {@link TribeDataStore}
     */
    public TribeDataStore createDiskDataStore() { return new DiskTribeDataStore(tribeCache); }

    /**
     * Create {@link TribeDataStore} to retrieve data from the Cloud.
     */
    public TribeDataStore createCloudDataStore() {
        return new CloudTribeDataStore(this.tribeCache, this.tribeApi, this.accessToken, this.context, this.simpleDateFormat);
    }
}
