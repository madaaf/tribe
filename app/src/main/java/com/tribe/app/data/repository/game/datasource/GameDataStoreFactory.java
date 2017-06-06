package com.tribe.app.data.repository.game.datasource;

import android.content.Context;
import com.tribe.app.data.network.FileApi;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class GameDataStoreFactory {

  private final Context context;
  private final FileApi fileApi;

  @Inject public GameDataStoreFactory(Context context, FileApi fileApi) {
    if (context == null) {
      throw new IllegalArgumentException("Constructor parameters cannot be null!");
    }

    this.context = context.getApplicationContext();
    this.fileApi = fileApi;
  }

  public GameDataStore createCloudDataStore() {
    return new CloudGameDataStore(fileApi);
  }
}
