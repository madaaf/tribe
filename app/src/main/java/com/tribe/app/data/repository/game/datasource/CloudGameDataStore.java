package com.tribe.app.data.repository.game.datasource;

import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.data.network.FileApi;
import com.tribe.tribelivesdk.game.Game;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.Observable;

public class CloudGameDataStore implements GameDataStore {

  private final FileApi fileApi;
  private final Preference<String> gameData;
  private final Gson gson;

  public CloudGameDataStore(FileApi fileApi, Preference<String> gameData) {
    this.fileApi = fileApi;
    this.gameData = gameData;
    this.gson = new GsonBuilder().create();
  }

  @Override public Observable<List<String>> getNamesForPostItGame() {
    return fileApi.getNamesForPostItGame();
  }

  @Override public Observable<List<String>> getDataForChallengeGame() {
    return fileApi.getDataForChallengesGame();
  }

  @Override public Observable<List<String>> getNamesForDrawGame() {
    return fileApi.getNamesForDrawGame();
  }

  @Override public Observable<Void> synchronizeGamesData() {
    return Observable.zip(fileApi.getNamesForPostItGame(), fileApi.getNamesForDrawGame(),
        fileApi.getDataForChallengesGame(), (dataPostIt, dataDraw, dataForChallengesGame) -> {
          Map<String, List<String>> mapData = new HashMap<>();
          Type type = new TypeToken<Map<String, List<String>>>() {
          }.getType();
          mapData.put(Game.GAME_POST_IT, dataPostIt);
          mapData.put(Game.GAME_DRAW, dataDraw);
          mapData.put(Game.GAME_CHALLENGE, dataForChallengesGame);
          gameData.set(gson.toJson(mapData, type));
          return null;
        });
  }
}
