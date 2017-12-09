package com.tribe.app.data.repository.game.datasource;

import android.content.Context;
import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.R;
import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.presentation.utils.StringUtils;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.Observable;

public class CloudGameDataStore implements GameDataStore {

  private final Context context;
  private final TribeApi tribeApi;
  private final FileApi fileApi;
  private final Preference<String> gameData;
  private final Gson gson;
  private final GameCache gameCache;

  public CloudGameDataStore(Context context, TribeApi tribeApi, FileApi fileApi,
      Preference<String> gameData, GameCache gameCache) {
    this.context = context;
    this.tribeApi = tribeApi;
    this.fileApi = fileApi;
    this.gameData = gameData;
    this.gson = new GsonBuilder().create();
    this.gameCache = gameCache;
  }

  @Override public Observable<Void> synchronizeGamesData() {
    final Map<String, List<String>> mapData = new HashMap<>();
    return getGames().flatMap(gameList -> Observable.from(gameList)).flatMap(gameRealm -> {
      if (!StringUtils.isEmpty(gameRealm.getDataUrl())) {
        return fileApi.getDataForUrl(gameRealm.getDataUrl())
            .map(strings -> mapData.put(gameRealm.getId(), strings));
      } else {
        return Observable.empty();
      }
    }).map(gameRealm -> {
      Type type = new TypeToken<Map<String, List<String>>>() {
      }.getType();
      gameData.set(gson.toJson(mapData, type));
      return null;
    });
  }

  @Override public Observable<List<GameRealm>> getGames() {
    String body = context.getString(R.string.games_infos);
    final String request = context.getString(R.string.query, body);
    return this.tribeApi.getGames(request)
        .doOnNext(gameList -> gameCache.putGames(gameList))
        .onErrorResumeNext(throwable -> Observable.just(gameCache.getGames()));
  }

  @Override
  public Observable<List<ScoreRealm>> getGameLeaderBoard(String gameId, boolean friendsOnly,
      int offset) {
    String body = context.getString(R.string.game_leaderboard, gameId,
        offset == 0 ? "" : "offset : " + offset, friendsOnly);
    final String request = context.getString(R.string.query, body);
    return this.tribeApi.getGameLeaderboard(request);
  }
}
