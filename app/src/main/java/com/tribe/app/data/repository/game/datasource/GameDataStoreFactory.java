package com.tribe.app.data.repository.game.datasource;

import android.content.Context;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.OpentdbApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.presentation.utils.preferences.GameData;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class GameDataStoreFactory {

  private final Context context;
  private final TribeApi tribeApi;
  private final FileApi fileApi;
  private final OpentdbApi opentdbApi;
  private final Preference<String> gameData;
  private final GameCache gameCache;
  private final AccessToken accessToken;
  private final UserCache userCache;

  @Inject public GameDataStoreFactory(Context context, TribeApi tribeApi, FileApi fileApi,
      OpentdbApi opentdbApi, @GameData Preference<String> gameData, GameCache gameCache,
      UserCache userCache, AccessToken accessToken) {
    if (context == null) {
      throw new IllegalArgumentException("Constructor parameters cannot be null!");
    }

    this.context = context.getApplicationContext();
    this.tribeApi = tribeApi;
    this.fileApi = fileApi;
    this.opentdbApi = opentdbApi;
    this.gameData = gameData;
    this.gameCache = gameCache;
    this.accessToken = accessToken;
    this.userCache = userCache;
  }

  public GameDataStore createCloudDataStore() {
    return new CloudGameDataStore(context, tribeApi, fileApi, opentdbApi, gameData, gameCache,
        userCache, accessToken);
  }

  public GameDataStore createDiskDataStore() {
    return new DiskGameDataStore(gameCache);
  }
}
