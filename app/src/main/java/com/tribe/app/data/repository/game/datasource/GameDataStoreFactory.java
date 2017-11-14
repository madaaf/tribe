package com.tribe.app.data.repository.game.datasource;

import android.content.Context;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.presentation.utils.preferences.GameData;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class GameDataStoreFactory {

  private final Context context;
  private final FileApi fileApi;
  private final Preference<String> gameData;

  @Inject public GameDataStoreFactory(Context context, FileApi fileApi,
      @GameData Preference<String> gameData) {
    if (context == null) {
      throw new IllegalArgumentException("Constructor parameters cannot be null!");
    }

    this.context = context.getApplicationContext();
    this.fileApi = fileApi;
    this.gameData = gameData;
  }

  public GameDataStore createCloudDataStore() {
    return new CloudGameDataStore(fileApi, gameData);
  }
}
