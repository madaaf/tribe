package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.GameRealm;
import io.realm.Realm;
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
        realm1.insertOrUpdate(gameRealmList);
      });
    } finally {
      obsRealm.close();
    }
  }

  @Override public List<GameRealm> getGames() {
    Realm newRealm = Realm.getDefaultInstance();
    List<GameRealm> gameRealmList = new ArrayList<>();
    RealmResults<GameRealm> results = newRealm.where(GameRealm.class).findAll();

    if (results != null) {
      gameRealmList.addAll(newRealm.copyFromRealm(results));
    }

    return gameRealmList;
  }
}
