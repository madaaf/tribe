package com.tribe.app.data.repository.live.datasource;

import android.content.Context;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.repository.user.datasource.DiskUserDataStore;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class LiveDataStoreFactory {

  private final Context context;
  private final TribeApi tribeApi;
  private final LiveCache liveCache;
  private final UserCache userCache;
  private final ContactCache contactCache;
  private final AccessToken accessToken;

  @Inject public LiveDataStoreFactory(Context context, TribeApi tribeApi, LiveCache liveCache,
      UserCache userCache, AccessToken accessToken, ContactCache contactCache) {
    if (context == null) {
      throw new IllegalArgumentException("Constructor parameters cannot be null!");
    }

    this.context = context.getApplicationContext();
    this.tribeApi = tribeApi;
    this.liveCache = liveCache;
    this.userCache = userCache;
    this.accessToken = accessToken;
    this.contactCache = contactCache;
  }

  public LiveDataStore createCloudDataStore() {
    return new CloudLiveDataStore(context, tribeApi, liveCache, userCache);
  }

  public LiveDataStore createDiskDataStore() {
    return new DiskLiveDataStore(liveCache);
  }

  public DiskUserDataStore createDiskUserDataStore() {
    return new DiskUserDataStore(userCache, liveCache, accessToken, contactCache);
  }
}
