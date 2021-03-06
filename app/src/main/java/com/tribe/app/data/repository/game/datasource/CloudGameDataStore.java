package com.tribe.app.data.repository.game.datasource;

import android.content.Context;
import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.R;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.GameCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.OpentdbApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.AddScoreEntity;
import com.tribe.app.data.network.entity.CategoryEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.GameFileRealm;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.data.realm.ScoreUserRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaCategoryEnum;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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
  private final ContactCache contactCache;
  private final AccessToken accessToken;

  public CloudGameDataStore(Context context, TribeApi tribeApi, FileApi fileApi,
      OpentdbApi opentdbApi, Preference<String> gameData, GameCache gameCache, UserCache userCache,
      AccessToken accessToken, ContactCache contactCache) {
    this.context = context;
    this.tribeApi = tribeApi;
    this.fileApi = fileApi;
    this.opentdbApi = opentdbApi;
    this.gameData = gameData;
    this.gson = new GsonBuilder().create();
    this.gameCache = gameCache;
    this.userCache = userCache;
    this.accessToken = accessToken;
    this.contactCache = contactCache;
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

  @Override public Observable<List<String>> synchronizeGameData(String gameId) {
    final Map<String, List<String>> mapData = getMapFromPrefs();
    List<GameRealm> gameRealmList = gameCache.getGames();
    GameRealm game = null;

    for (GameRealm gameRealm : gameRealmList) {
      if (gameRealm.getId().equals(gameId)) game = gameRealm;
    }

    if (game == null || !StringUtils.isEmpty(game.getDataUrl())) {
      return fileApi.getDataForUrl(game.getDataUrl())
          .map(gameDataEntity -> mapData.put(gameId, gameDataEntity.getData()))
          .map(gameRealm -> {
            Type type = new TypeToken<Map<String, List<String>>>() {
            }.getType();
            gameData.set(gson.toJson(mapData, type));
            return mapData.get(gameId);
          });
    } else {
      return Observable.empty();
    }
  }

  private Map<String, List<String>> getMapFromPrefs() {
    if (gameData == null || gameData.get() == null) return new HashMap<>();
    return new Gson().fromJson(gameData.get(), new TypeToken<HashMap<String, List<String>>>() {
    }.getType());
  }

  @Override public Observable<List<GameRealm>> getGames() {
    String body = context.getString(R.string.games_infos);
    final String request = context.getString(R.string.query, body);
    return this.tribeApi.getGames(request)
        .doOnNext(gameCache::putGames)
        .onErrorResumeNext(throwable -> Observable.just(gameCache.getGames()))
        .doOnError(Throwable::printStackTrace);
  }

  @Override
  public Observable<List<ScoreRealm>> getGameLeaderBoard(String gameId, List<Contact> usersId) {
    List<Object> contactInterfaces = new ArrayList<>();

    List<ShortcutRealm> singleShortcuts = userCache.singleShortcutsNoObs();
    String[] userIds = new String[singleShortcuts.size() + 1];

    for (int i = 0; i < singleShortcuts.size(); i++) {
      ShortcutRealm shortcutRealm = singleShortcuts.get(i);
      userIds[i] = shortcutRealm.getSingleFriend().getId();
      contactInterfaces.add(shortcutRealm.getSingleFriend());
    }

    userIds[singleShortcuts.size()] = accessToken.getUserId();
    contactInterfaces.add(accessToken.getUserId());

    List<String> finalList = new ArrayList<>(Arrays.asList(userIds));
    if (usersId != null) {
      for (Contact c : usersId) {
        // finalList.add(c.getId());
        //  contactInterfaces.add(c);
      }
    }

    List<ContactABRealm> contactsAddressBookList = contactCache.getContactsAddressBook();
    if (contactsAddressBookList != null) {
      for (ContactABRealm contactABRealm : contactsAddressBookList) {
        if (contactABRealm.getUsers() != null
            && !contactABRealm.getUsers().isEmpty()
            && contactABRealm.getUsers().get(0) != null) {
          finalList.add(contactABRealm.getUsers().get(0).getId());
          contactABRealm.setId(contactABRealm.getUsers().get(0).getId());
          contactInterfaces.add(contactABRealm);
        }
      }
    }

    List<ContactFBRealm> contactsFacebookList = contactCache.getContactsFacebook();
    if (contactsFacebookList != null) {
      for (ContactFBRealm contactFBRealm : contactsFacebookList) {

        if (contactFBRealm.getUsers() != null
            && !contactFBRealm.getUsers().isEmpty()
            && contactFBRealm.getUsers().get(0) != null) {
          finalList.add(contactFBRealm.getUsers().get(0).getId());
          contactFBRealm.setId(contactFBRealm.getUsers().get(0).getId());
          contactInterfaces.add(contactFBRealm);
        }
      }
    }

    String[] list = new String[finalList.size()];
    list = finalList.toArray(list);

    String body = context.getString(R.string.game_leaderboard, gameId, JsonUtils.arrayToJson(list));
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

      for (int j = 0; j < contactInterfaces.size(); j++) {
        Object o = contactInterfaces.get(j);
        if (o instanceof ContactABRealm) {
          ContactABRealm c = (ContactABRealm) o;
          ScoreUserRealm scoreUser =
              new ScoreUserRealm(c.getId(), c.getName(), c.getFirstName(), "");
          scoreRealmList.get(j).setUser(scoreUser);
        }

        if (o instanceof ContactFBRealm) {
          ContactFBRealm c = (ContactFBRealm) o;
          ScoreUserRealm scoreUser =
              new ScoreUserRealm(c.getId(), c.getName(), c.getName(), c.getProfilePicture());
          scoreRealmList.get(j).setUser(scoreUser);
        }
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
    if (score == 0) return Observable.just(null);
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

    if (DeviceUtils.getLanguage(context).equals("fr")) {
      return fileApi.getTriviaData()
          .map(triviaCategoriesHolders -> triviaCategoriesHolders.getCategories())
          .compose(listCategoryMapTransformer)
          .doOnNext(stringListMap -> gameCache.setTriviaData(stringListMap))
          .doOnError(Throwable::printStackTrace);
    } else {
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

  @Override public Observable<ScoreRealm> getUserBestScore(String gameId) {
    String userIdsListFormated = "\"" + accessToken.getUserId() + "\"";
    return this.tribeApi.getUserListInfos(
        context.getString(R.string.lookup_userid, userIdsListFormated,
            context.getString(R.string.userfragment_leaderboard,
                JsonUtils.arrayToJson(GameManager.playableGames)))).map(userRealms -> {
      List<ScoreRealm> realmList = new ArrayList<>();
      realmList.addAll(userRealms.get(0).getScores());
      return realmList;
    }).doOnError(Throwable::printStackTrace).map(scoreRealmList -> {
      for (ScoreRealm scoreRealm : scoreRealmList) {
        if (scoreRealm.getGame_id().equals(gameId)) return scoreRealm;
      }

      return null;
    });
  }
}
