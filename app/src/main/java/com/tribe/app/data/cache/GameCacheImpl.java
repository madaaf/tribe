package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by tiago on 01/27/2017.
 */
public class GameCacheImpl implements GameCache {

  private Context context;

  @Inject public GameCacheImpl(Context context) {
    this.context = context;
  }

  @Override public void putGames(List<GameRealm> gameRealmList) {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.executeTransaction(realm1 -> {
        realm1.delete(GameRealm.class);

        for (GameRealm gameRealm : gameRealmList) {
          if (gameRealm.getScores() != null && gameRealm.getScores().size() > 0) {
            RealmList<ScoreRealm> newList = new RealmList<>();
            for (ScoreRealm scoreRealm : gameRealm.getScores()) {
              realm1.insertOrUpdate(scoreRealm);
              newList.add(
                  realm1.where(ScoreRealm.class).equalTo("id", scoreRealm.getId()).findFirst());
            }

            gameRealm.setFriends_score(newList);
            realm1.insertOrUpdate(gameRealm);
          }
        }
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

  @Override public List<ScoreRealm> getGameLeaderboard(String gameId, boolean friendsOnly) {
    Realm newRealm = Realm.getDefaultInstance();

    try {
      GameRealm gameRealm = newRealm.where(GameRealm.class).equalTo("id", gameId).findFirst();
      List<ScoreRealm> results = new ArrayList<>();

      if (gameRealm != null) {
        if (friendsOnly && gameRealm.getFriends_score() != null) {
          results.addAll(newRealm.copyFromRealm(gameRealm.getFriends_score()));
        } else if (!friendsOnly && gameRealm.getOverall_score() != null) {
          results.addAll(newRealm.copyFromRealm(gameRealm.getOverall_score()));
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
    Realm newRealm = Realm.getDefaultInstance();

    try {
      newRealm.executeTransaction(realm -> {
        realm.where(ScoreRealm.class).equalTo("user.id", userId).findAll().deleteAllFromRealm();
        realm.insertOrUpdate(scoreRealmList);
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

      RealmResults<ScoreRealm> scoreRealmResults = newRealm.where(ScoreRealm.class).equalTo("user.id", userId).findAll();
      if (scoreRealmResults.size() > 0) results.addAll(newRealm.copyFromRealm(scoreRealmResults));

      return results;
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      newRealm.close();
    }

    return null;
  }
}
