package com.tribe.app.data.repository.marvel.datasource;

import android.content.Context;

import com.tribe.app.data.cache.FriendshipCache;
import com.tribe.app.data.cache.MarvelCache;
import com.tribe.app.data.network.MarvelApi;
import com.tribe.app.data.network.TribeApi;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory that creates different implementations of {@link MarvelDataStore}.
 */
@Singleton
public class MarvelDataStoreFactory {

    private final Context context;
    private final MarvelCache marvelCache;
    private final MarvelApi marvelApi;

    @Inject
    public MarvelDataStoreFactory(Context context, MarvelCache marvelCache, MarvelApi marvelApi) {
        if (context == null || marvelCache == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!");
        }
        this.context = context.getApplicationContext();
        this.marvelCache = marvelCache;
        this.marvelApi = marvelApi;
    }

    public MarvelDataStore createDiskDataStore() {
        return new DiskMarvelDataStore(marvelCache);
    }

    public MarvelDataStore createCloudDataStore() {
        return new CloudMarvelDataStore(this.marvelCache, this.marvelApi);
    }
}
