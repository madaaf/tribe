package com.tribe.app.data.cache;

import android.content.Context;
import android.util.Pair;
import com.tribe.app.data.realm.GameFileRealm;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.presentation.utils.FileUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tiago on 01/27/2017.
 */
public class GameCacheImpl implements GameCache {

  private Context context;
  private Realm mainRealm;
  private Map<String, List<TriviaQuestion>> mapTrivia;
  private Map<String, BattleMusicPlaylist> mapBattleMusic;

  @Inject public GameCacheImpl(Context context, Realm realm) {
    this.context = context;
    mapTrivia = new HashMap<>();
    mapBattleMusic = new HashMap<>();
    mainRealm = realm;
  }

  @Override public void putGames(List<GameRealm> gameRealmList) {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.executeTransaction(realm1 -> {
        realm1.delete(GameRealm.class);
        realm1.delete(GameFileRealm.class);

        for (GameRealm gameRealm : gameRealmList) {
          if (gameRealm.getFriendLeader() != null) {
            gameRealm.setFriendLeaderScoreUser(gameRealm.getFriendLeader().getUser());
            gameRealm.getFriendLeaderScoreUser().setValue(gameRealm.getFriendLeader().getValue());
            gameRealm.getFriendLeaderScoreUser()
                .setRanking(gameRealm.getFriendLeader().getRanking());
          }

          if (gameRealm.get__typename().equals(GameRealm.GAME_CORONA)) {
            GameFileRealm gameFileRealm =
                realm1.where(GameFileRealm.class).equalTo("gameId", gameRealm.getId()).findFirst();

            if (gameFileRealm == null || !gameFileRealm.getUrl().equals(gameRealm.getUrl())) {
              if (gameFileRealm != null) {
                gameFileRealm.deleteFromRealm();
                FileUtils.delete(context, gameFileRealm.getUrl(), FileUtils.ZIP);
              }

              gameFileRealm = realm1.createObject(GameFileRealm.class, gameRealm.getUrl());
              gameFileRealm.setDownloadStatus(GameFileRealm.STATUS_PENDING);
              gameFileRealm.setGameId(gameRealm.getId());
              realm1.insert(gameFileRealm);
            }
          }
        }

        realm1.insertOrUpdate(gameRealmList);
      });
    } finally {
      obsRealm.close();
    }
  }

  @Override public List<GameRealm> getGames() {
    Realm newRealm = Realm.getDefaultInstance();
    List<GameRealm> gameRealmList = new ArrayList<>();

    try {
      RealmResults<GameRealm> results = newRealm.where(GameRealm.class).findAll();

      if (results != null) {
        gameRealmList.addAll(newRealm.copyFromRealm(results));
      }
    } finally {
      newRealm.close();
    }

    return gameRealmList;
  }

  @Override public void updateLeaderboard(String gameId, boolean friendsOnly,
      List<ScoreRealm> scoreRealmList) {
    Realm newRealm = Realm.getDefaultInstance();

    try {
      newRealm.executeTransaction(realm -> {
        GameRealm gameRealm = realm.where(GameRealm.class).equalTo("id", gameId).findFirst();

        if (gameRealm != null) {
          RealmList<ScoreRealm> newList = new RealmList<>();
          for (ScoreRealm scoreRealm : scoreRealmList) {
            realm.insertOrUpdate(scoreRealm);
            newList.add(
                realm.where(ScoreRealm.class).equalTo("id", scoreRealm.getId()).findFirst());
          }

          if (friendsOnly) {
            RealmList<ScoreRealm> friendsScore = gameRealm.getFriends_score();
            friendsScore.deleteAllFromRealm();
            gameRealm.setFriends_score(newList);
          } else {
            RealmList<ScoreRealm> overallScore = gameRealm.getOverall_score();
            overallScore.deleteAllFromRealm();
            gameRealm.setOverall_score(newList);
          }
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      newRealm.close();
    }
  }

  @Override public List<ScoreRealm> getGameLeaderboard(String gameId) {
    Realm newRealm = Realm.getDefaultInstance();

    try {
      GameRealm gameRealm = newRealm.where(GameRealm.class).equalTo("id", gameId).findFirst();
      List<ScoreRealm> results = new ArrayList<>();

      if (gameRealm != null) {
        if (gameRealm.getFriends_score() != null) {
          results.addAll(newRealm.copyFromRealm(gameRealm.getFriends_score()));
        }
      }

      return results;
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      newRealm.close();
    }

    return null;
  }

  @Override public void updateLeaderboard(String userId, List<ScoreRealm> scoreRealmList) {
    if (scoreRealmList == null || scoreRealmList.size() == 0) return;

    Realm newRealm = Realm.getDefaultInstance();

    try {
      newRealm.executeTransaction(realm -> {
        realm.where(ScoreRealm.class).equalTo("user.id", userId).findAll().deleteAllFromRealm();
        UserRealm userRealm = realm.where(UserRealm.class).equalTo("id", userId).findFirst();
        realm.insertOrUpdate(scoreRealmList);

        if (userRealm != null) {
          RealmList scoresRealm = new RealmList();
          for (ScoreRealm scoreRealm : scoreRealmList) {
            scoresRealm.add(
                realm.where(ScoreRealm.class).equalTo("id", scoreRealm.getId()).findFirst());
          }

          userRealm.setScores(scoresRealm);
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      newRealm.close();
    }
  }

  @Override public List<ScoreRealm> getUserLeaderboard(String userId) {
    Realm newRealm = Realm.getDefaultInstance();

    try {
      List<ScoreRealm> results = new ArrayList<>();

      RealmResults<ScoreRealm> scoreRealmResults =
          newRealm.where(ScoreRealm.class).equalTo("user.id", userId).findAll();
      if (scoreRealmResults.size() > 0) results.addAll(newRealm.copyFromRealm(scoreRealmResults));

      return results;
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      newRealm.close();
    }

    return null;
  }

  @Override public void setTriviaData(Map<String, List<TriviaQuestion>> mapTrivia) {
    this.mapTrivia.clear();
    this.mapTrivia.putAll(mapTrivia);
  }

  @Override public Map<String, List<TriviaQuestion>> getTriviaData() {
    return mapTrivia;
  }

  @Override public void setBattleMusicData(Map<String, BattleMusicPlaylist> mapBattleMusic) {
    this.mapBattleMusic.clear();
    this.mapBattleMusic.putAll(mapBattleMusic);
  }

  @Override public Map<String, BattleMusicPlaylist> getBattleMusicData() {
    return mapBattleMusic;
  }

  @Override public Observable<List<GameFileRealm>> getFilesToDownload() {
    return mainRealm.where(GameFileRealm.class)
        .equalTo("downloadStatus", GameFileRealm.STATUS_TO_DOWNLOAD)
        .findAll()
        .asObservable()
        .filter(gameFileRealmList -> gameFileRealmList.isLoaded())
        .map(gameFileRealmList -> mainRealm.copyFromRealm(gameFileRealmList))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public void updateGameFiles(Map<String, List<Pair<String, Object>>> updateList) {
    Realm newRealm = Realm.getDefaultInstance();

    try {
      newRealm.executeTransaction(realm -> {
        for (String key : updateList.keySet()) {
          GameFileRealm gameFileRealm =
              newRealm.where(GameFileRealm.class).equalTo("url", key).findFirst();
          if (gameFileRealm != null) {
            updateSingleGameFile(gameFileRealm, updateList.get(key));
          }
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      newRealm.close();
    }
  }

  @Override public void updateGameFiles(String url, List<Pair<String, Object>> updateList) {
    Realm newRealm = Realm.getDefaultInstance();

    try {
      newRealm.executeTransaction(realm -> {
        GameFileRealm gameFileRealm =
            newRealm.where(GameFileRealm.class).equalTo("url", url).findFirst();
        if (gameFileRealm != null) {
          updateSingleGameFile(gameFileRealm, updateList);
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      newRealm.close();
    }
  }

  private void updateSingleGameFile(GameFileRealm gameFileRealm,
      List<Pair<String, Object>> pairList) {
    for (Pair<String, Object> pair : pairList) {
      if (pair.first.equals(GameFileRealm.DOWNLOAD_STATUS)) {
        gameFileRealm.setDownloadStatus((String) pair.second);
      } else if (pair.first.equals(GameFileRealm.PATH)) {
        gameFileRealm.setPath((String) pair.second);
      } else if (pair.first.equals(GameFileRealm.PROGRESS)) {
        gameFileRealm.setProgress(((Long) pair.second).intValue());
      } else if (pair.first.equals(GameFileRealm.TOTAL_SIZE)) {
        gameFileRealm.setTotalSize(((Long) pair.second).intValue());
      }
    }
  }

  @Override public GameFileRealm getGameFile(String url) {
    Realm newRealm = Realm.getDefaultInstance();

    try {
      GameFileRealm gameFileRealm =
          newRealm.where(GameFileRealm.class).equalTo("url", url).findFirst();
      if (gameFileRealm != null) {
        return newRealm.copyFromRealm(gameFileRealm);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      newRealm.close();
    }

    return null;
  }

  @Override public Observable<GameFileRealm> getGameFileObs(String url) {
    return mainRealm.where(GameFileRealm.class)
        .equalTo("url", url)
        .findAll()
        .asObservable()
        .map(gameFileRealmList -> mainRealm.copyFromRealm(gameFileRealmList.get(0)))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }
}
