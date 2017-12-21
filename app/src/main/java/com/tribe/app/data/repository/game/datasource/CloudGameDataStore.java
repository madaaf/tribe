package com.tribe.app.data.repository.game.datasource;

import android.content.Context;
import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.R;
import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.OpentdbApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.AddScoreEntity;
import com.tribe.app.data.network.entity.CategoryEntity;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.trivia.TriviaCategoryEnum;
import com.tribe.app.domain.entity.trivia.TriviaQuestions;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.Observable;

public class CloudGameDataStore implements GameDataStore {

  private final Context context;
  private final TribeApi tribeApi;
  private final FileApi fileApi;
  private final OpentdbApi opentdbApi;
  private final Preference<String> gameData;
  private final Gson gson;
  private final GameCache gameCache;

  public CloudGameDataStore(Context context, TribeApi tribeApi, FileApi fileApi,
      OpentdbApi opentdbApi, Preference<String> gameData, GameCache gameCache) {
    this.context = context;
    this.tribeApi = tribeApi;
    this.fileApi = fileApi;
    this.opentdbApi = opentdbApi;
    this.gameData = gameData;
    this.gson = new GsonBuilder().create();
    this.gameCache = gameCache;
  }

  @Override public Observable<Void> synchronizeGamesData() {
    final Map<String, List<String>> mapData = new HashMap<>();
    return getGames().flatMap(gameList -> Observable.from(gameList)).flatMap(gameRealm -> {
      if (!StringUtils.isEmpty(gameRealm.getDataUrl())) {
        return fileApi.getDataForUrl(gameRealm.getDataUrl())
            .map(gameDataEntity -> mapData.put(gameRealm.getId(), gameDataEntity.getData()));
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
        .onErrorResumeNext(throwable -> Observable.just(gameCache.getGames()))
        .doOnError(Throwable::printStackTrace);
  }

  @Override
  public Observable<List<ScoreRealm>> getGameLeaderBoard(String gameId, boolean friendsOnly,
      int limit, int offset) {
    String body = context.getString(R.string.game_leaderboard, gameId, "" + limit,
        offset == 0 ? "" : "offset : " + offset, friendsOnly);
    final String request = context.getString(R.string.query, body);
    return this.tribeApi.getLeaderboard(request).doOnNext(scoreRealmList -> {
      for (ScoreRealm scoreRealm : scoreRealmList) {
        scoreRealm.setGame_id(gameId);
      }

      if (offset == 0) {
        gameCache.updateLeaderboard(gameId, friendsOnly,
            scoreRealmList); // We only save the first page
      }
    }).doOnError(Throwable::printStackTrace);
  }

  @Override public Observable<List<ScoreRealm>> getUserLeaderboard(String userId) {
    String userIdsListFormated = "\"" + userId + "\"";
    return this.tribeApi.getUserListInfos(
        context.getString(R.string.lookup_userid, userIdsListFormated,
            context.getString(R.string.userfragment_infos_light)))
        .map(userRealms -> {
          List<ScoreRealm> realmList = new ArrayList<>();
          realmList.addAll(userRealms.get(0).getScores());
          return realmList;
        })
        .doOnError(Throwable::printStackTrace)
        .doOnNext(scoreRealmList -> gameCache.updateLeaderboard(userId, scoreRealmList));
  }

  @Override public Observable<AddScoreEntity> addScore(String gameId, Integer score) {
    String body = context.getString(R.string.addScore, gameId, "" + score);
    final String request = context.getString(R.string.mutation, body);
    return this.tribeApi.addScore(request);
  }

  @Override public Observable<List<ScoreRealm>> getFriendsScore(String gameId) {
    return this.tribeApi.getUserInfos(context.getString(R.string.friends_scores,
        context.getString(R.string.userfragment_infos_game),
        context.getString(R.string.shortcutFragment_infos_game)))
        .doOnError(Throwable::printStackTrace)
        .map(userRealm -> {
          List<ScoreRealm> scoreRealmList = new ArrayList<>();

          for (ShortcutRealm shortcutRealm : userRealm.getShortcuts()) {
            ScoreRealm scoreRealm = shortcutRealm.getSingleFriend().getScoreForGame(gameId);
            if (scoreRealm != null) scoreRealmList.add(scoreRealm);
          }

          return scoreRealmList;
        })
        .doOnNext(scoreRealmList -> gameCache.updateLeaderboard(gameId, true, scoreRealmList));
  }

  @Override public Observable<List<TriviaQuestions>> getTriviaData() {
    if (DeviceUtils.getLanguage(context).equals("en")) {
      return Observable.just(TriviaCategoryEnum.getCategories())
          .flatMap(categoryList -> Observable.from(categoryList))
          .flatMap(category -> {
            if (category.getCategory() == TriviaCategoryEnum.CELEBS.getCategory()) {
              return opentdbApi.getCategory(category.getId(), 36).map(questionsList -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(category.getCategory());
                categoryEntity.setQuestions(questionsList);
                return categoryEntity;
              });
            } else {
              return opentdbApi.getCategory(category.getId(), 100).map(questionsList -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(category.getCategory());
                categoryEntity.setQuestions(questionsList);
                return categoryEntity;
              });
            }
          })
          .toList()
          .map(lists -> {
            Map<String, List<TriviaQuestions>> result = new HashMap<>();

            for (CategoryEntity categoryEntity : lists) {
              result.put(categoryEntity.getId(), categoryEntity.getQuestions());
            }

            return (List<TriviaQuestions>) new ArrayList();
          })
          .doOnError(Throwable::printStackTrace);
    } else {
      return fileApi.getTriviaData().map(triviaCategoriesHolders -> {
        return (List<TriviaQuestions>) new ArrayList<TriviaQuestions>();
      }).doOnError(Throwable::printStackTrace);
    }
  }
}
