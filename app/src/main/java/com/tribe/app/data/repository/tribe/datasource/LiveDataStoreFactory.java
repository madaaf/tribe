package com.tribe.app.data.repository.tribe.datasource;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory that creates different implementations of {@link LiveDataStoreFactory}.
 */
@Singleton
public class LiveDataStoreFactory {

    private final Context context;

    @Inject
    public LiveDataStoreFactory(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!");
        }

        this.context = context.getApplicationContext();
    }

    /**
     * Create {@link LiveDataStore} to retrieve data from the Cloud.
     */
    public LiveDataStore createCloudDataStore() {
        return new CloudLiveDataStore();
    }
}
