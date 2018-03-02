package com.tribe.app.data.repository.game.datasource;

import android.content.Context;
import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.R;
import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.OpentdbApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.AddScoreEntity;
import com.tribe.app.data.network.entity.CategoryEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.GameFileRealm;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.data.realm.ScoreUserRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaCategoryEnum;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.util.JsonUtils;
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
  private final UserCache userCache;
  private final AccessToken accessToken;

  public CloudGameDataStore(Context context, TribeApi tribeApi, FileApi fileApi,
      OpentdbApi opentdbApi, Preference<String> gameData, GameCache gameCache, UserCache userCache,
      AccessToken accessToken) {
    this.context = context;
    this.tribeApi = tribeApi;
    this.fileApi = fileApi;
    this.opentdbApi = opentdbApi;
    this.gameData = gameData;
    this.gson = new GsonBuilder().create();
    this.gameCache = gameCache;
    this.userCache = userCache;
    this.accessToken = accessToken;
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

  @Override public Observable<List<ScoreRealm>> getGameLeaderBoard(String gameId) {
    List<ShortcutRealm> singleShortcuts = userCache.singleShortcutsNoObs();
    String[] userIds = new String[singleShortcuts.size() + 1];

    for (int i = 0; i < singleShortcuts.size(); i++) {
      ShortcutRealm shortcutRealm = singleShortcuts.get(i);
      userIds[i] = shortcutRealm.getSingleFriend().getId();
    }

    userIds[singleShortcuts.size()] = accessToken.getUserId();

    String body =
        context.getString(R.string.game_leaderboard, gameId, JsonUtils.arrayToJson(userIds));
    final String request = context.getString(R.string.query, body);

    return this.tribeApi.getLeaderboard(request).doOnNext(scoreRealmList -> {
      int i = 0;
      ScoreUserRealm scoreUserRealm;
      for (ScoreRealm scoreRealm : scoreRealmList) {
        scoreRealm.setGame_id(gameId);
        if (i < singleShortcuts.size()) {
          UserRealm userRealm = singleShortcuts.get(i).getSingleFriend();
          scoreUserRealm = new ScoreUserRealm(userRealm.getId(), userRealm.getDisplayName(),
              userRealm.getUsername(), userRealm.getProfilePicture());
          scoreRealm.setUser(scoreUserRealm);
        }
        i++;
      }

      gameCache.updateLeaderboard(gameId, scoreRealmList);
    }).doOnError(Throwable::printStackTrace);
  }

  @Override public Observable<List<ScoreRealm>> getUserLeaderboard(String userId) {
    String userIdsListFormated = "\"" + userId + "\"";
    return this.tribeApi.getUserListInfos(
        context.getString(R.string.lookup_userid, userIdsListFormated,
            context.getString(R.string.userfragment_leaderboard,
                JsonUtils.arrayToJson(GameManager.playableGames))))
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

  @Override public Observable<Map<String, List<TriviaQuestion>>> getTriviaData() {
    if (gameCache.getTriviaData() != null && gameCache.getTriviaData().size() > 0) {
      return Observable.just(gameCache.getTriviaData());
    }

    if (DeviceUtils.getLanguage(context).equals("en")) {
      return Observable.just(TriviaCategoryEnum.getCategories())
          .flatMap(categoryList -> Observable.from(categoryList))
          .flatMap(category -> {
            int count = 100;
            if (category.getCategory() == TriviaCategoryEnum.CELEBS.getCategory()) {
              count = 36;
            }

            return opentdbApi.getCategory(category.getId(), count)
                .compose(new TriviaQuestionsTransformer(category.getCategory()));
          })
          .toList()
          .compose(listCategoryMapTransformer)
          .doOnNext(stringListMap -> gameCache.setTriviaData(stringListMap))
          .doOnError(Throwable::printStackTrace);
    } else {
      return fileApi.getTriviaData()
          .map(triviaCategoriesHolders -> triviaCategoriesHolders.getCategories())
          .compose(listCategoryMapTransformer)
          .doOnNext(stringListMap -> gameCache.setTriviaData(stringListMap))
          .doOnError(Throwable::printStackTrace);
    }
  }

  class TriviaQuestionsTransformer
      implements Observable.Transformer<List<TriviaQuestion>, CategoryEntity> {

    private String category;

    public TriviaQuestionsTransformer(String category) {
      this.category = category;
    }

    @Override
    public Observable<CategoryEntity> call(Observable<List<TriviaQuestion>> listObservable) {
      return listObservable.map(questionsList -> {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(category);
        categoryEntity.setQuestions(questionsList);
        return categoryEntity;
      });
    }
  }

  private Observable.Transformer<List<CategoryEntity>, Map<String, List<TriviaQuestion>>>
      listCategoryMapTransformer = listObservable -> listObservable.map(lists -> {
    Map<String, List<TriviaQuestion>> result = new HashMap<>();

    for (CategoryEntity categoryEntity : lists) {
      result.put(categoryEntity.getId(), categoryEntity.getQuestions());
    }

    return result;
  });

  @Override public Observable<Map<String, BattleMusicPlaylist>> getBattleMusicData() {
    if (gameCache.getBattleMusicData() != null && gameCache.getBattleMusicData().size() > 0) {
      return Observable.just(gameCache.getBattleMusicData());
    }

    return fileApi.getBattleMusicData()
        .compose(listBattleMusicMapTransformer)
        .doOnNext(stringListMap -> gameCache.setBattleMusicData(stringListMap))
        .doOnError(Throwable::printStackTrace);
  }

  private Observable.Transformer<List<BattleMusicPlaylist>, Map<String, BattleMusicPlaylist>>
      listBattleMusicMapTransformer = listObservable -> listObservable.map(lists -> {
    Map<String, BattleMusicPlaylist> result = new HashMap<>();

    for (BattleMusicPlaylist battleMusicPlaylist : lists) {
      result.put(battleMusicPlaylist.getTitle(), battleMusicPlaylist);
    }

    return result;
  });

  @Override public Observable<GameFileRealm> getGameFile(String url) {
    return null;
  }
}
