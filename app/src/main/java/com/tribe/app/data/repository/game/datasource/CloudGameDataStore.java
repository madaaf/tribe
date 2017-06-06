package com.tribe.app.data.repository.game.datasource;

import com.google.gson.JsonObject;
import com.tribe.app.data.network.FileApi;
import java.util.List;
import rx.Observable;

public class CloudGameDataStore implements GameDataStore {

  private final FileApi fileApi;

  public CloudGameDataStore(FileApi fileApi) {
    this.fileApi = fileApi;
  }

  @Override public Observable<List<String>> getNamesForPostItGame() {
    return fileApi.getNamesForPostItGame();
  }
}
