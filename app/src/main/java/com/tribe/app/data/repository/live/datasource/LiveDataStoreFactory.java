package com.tribe.app.data.repository.live.datasource;

import android.content.Context;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.network.TribeApi;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class LiveDataStoreFactory {

  private final Context context;
  private final TribeApi tribeApi;
  private final LiveCache liveCache;

  @Inject public LiveDataStoreFactory(Context context, TribeApi tribeApi, LiveCache liveCache) {
    if (context == null) {
      throw new IllegalArgumentException("Constructor parameters cannot be null!");
    }

    this.context = context.getApplicationContext();
    this.tribeApi = tribeApi;
    this.liveCache = liveCache;
  }

  public LiveDataStore createCloudDataStore() {
    return new CloudLiveDataStore(context, tribeApi, liveCache);
  }

  public LiveDataStore createDiskDataStore() {
    return new DiskLiveDataStore(liveCache);
  }
}
