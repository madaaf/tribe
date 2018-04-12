package com.tribe.app.data.repository.game;

import com.tribe.app.BuildConfig;
import com.tribe.app.data.network.entity.AddScoreEntity;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.mapper.GameRealmDataMapper;
import com.tribe.app.data.realm.mapper.ScoreRealmDataMapper;
import com.tribe.app.data.repository.game.datasource.GameDataStore;
import com.tribe.app.data.repository.game.datasource.GameDataStoreFactory;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.GameFile;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.domain.interactor.game.GameRepository;
import com.tribe.tribelivesdk.game.Game;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;
import timber.log.Timber;

@Singleton public class CloudGameDataRepository implements GameRepository {

  private final GameDataStoreFactory dataStoreFactory;
  private final GameRealmDataMapper gameRealmDataMapper;
  private final ScoreRealmDataMapper scoreRealmDataMapper;

  @Inject public CloudGameDataRepository(GameDataStoreFactory dataStoreFactory,
      GameRealmDataMapper gameRealmDataMapper, ScoreRealmDataMapper scoreRealmDataMapper) {
    this.dataStoreFactory = dataStoreFactory;
    this.gameRealmDataMapper = gameRealmDataMapper;
    this.scoreRealmDataMapper = scoreRealmDataMapper;
  }

  @Override public Observable<Void> synchronizeGamesData(String lang) {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.synchronizeGamesData();
  }

  @Override public Observable<List<String>> synchronizeGameData(String lang, String gameId) {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.synchronizeGameData(gameId);
  }

  @Override public Observable<List<Game>> getGames() {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.getGames().map(gameRealmList -> {
      List<GameRealm> filtred = new ArrayList<>();
      for (GameRealm g : gameRealmList) {
        boolean ignored = (BuildConfig.VERSION_CODE < g.getMin_android_version());
        if (!ignored) filtred.add(g);
      }
      return gameRealmDataMapper.transform(filtred);
    }).doOnError(Timber::e);
  }

  @Override
  public Observable<List<Score>> getGameLeaderBoard(String gameId, List<Contact> usersId) {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.getGameLeaderBoard(gameId, usersId)
        .map(scoreRealmList -> scoreRealmDataMapper.transform(scoreRealmList));
  }

  @Override public Observable<List<Score>> getUserLeaderboard(String userId) {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.getUserLeaderboard(userId)
        .map(scoreRealmList -> scoreRealmDataMapper.transform(scoreRealmList));
  }

  @Override public Observable<AddScoreEntity> addScore(String gameId, Integer score) {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.addScore(gameId, score);
  }

  @Override public Observable<List<Score>> getFriendsScores(String gameId) {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.getFriendsScore(gameId)
        .map(scoreRealmList -> scoreRealmDataMapper.transform(scoreRealmList));
  }

  @Override public Observable<Map<String, List<TriviaQuestion>>> getTriviaData() {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.getTriviaData();
  }

  @Override public Observable<Map<String, BattleMusicPlaylist>> getBattleMusicData() {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.getBattleMusicData();
  }

  @Override public Observable<GameFile> getGameFile(String url) {
    return null;
  }

  @Override public Observable<Score> getUserBestScore(String gameId) {
    GameDataStore gameDataStore = dataStoreFactory.createCloudDataStore();
    return gameDataStore.getUserBestScore(gameId)
        .map(scoreRealm -> scoreRealmDataMapper.transform(scoreRealm));
  }
}
