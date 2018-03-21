package com.tribe.app.presentation.mvp.presenter;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.GameFile;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.game.GetBattleMusicData;
import com.tribe.app.domain.interactor.game.GetCloudFriendsScores;
import com.tribe.app.domain.interactor.game.GetCloudGameLeaderboard;
import com.tribe.app.domain.interactor.game.GetCloudGames;
import com.tribe.app.domain.interactor.game.GetCloudUserLeaderboard;
import com.tribe.app.domain.interactor.game.GetDiskFriendsScores;
import com.tribe.app.domain.interactor.game.GetDiskGameLeaderboard;
import com.tribe.app.domain.interactor.game.GetDiskUserLeaderboard;
import com.tribe.app.domain.interactor.game.GetGameData;
import com.tribe.app.domain.interactor.game.GetGameFile;
import com.tribe.app.domain.interactor.game.GetGamesData;
import com.tribe.app.domain.interactor.game.GetTriviaData;
import com.tribe.app.domain.interactor.game.GetUserBestScore;
import com.tribe.app.domain.interactor.user.SynchroContactList;
import com.tribe.app.presentation.mvp.view.GameMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import timber.log.Timber;

public class GamePresenter implements Presenter {

  // COMPOSITE PRESENTERS

  // VIEW ATTACHED
  private GameMVPView gameMVPView;

  // VARIABLES
  private GameManager gameManager;

  // USECASES
  private GetCloudGames getGames;
  private GetCloudGameLeaderboard cloudGameLeaderboard;
  private GetDiskGameLeaderboard diskGameLeaderboard;
  private GetCloudUserLeaderboard cloudUserLeaderboard;
  private GetDiskUserLeaderboard diskUserLeaderboard;
  private GetCloudFriendsScores cloudFriendsScores;
  private GetDiskFriendsScores diskFriendsScores;
  private GetTriviaData getTriviaData;
  private GetBattleMusicData getBattleMusicData;
  private RxFacebook rxFacebook;
  private GetGamesData getGamesData;
  private GetGameFile getGameFile;
  private GetGameData getGameData;
  private GetUserBestScore getUserBestScore;
  private SynchroContactList synchroContactList;


  // SUBSCRIBERS
  private GameLeaderboardSubscriber cloudGameLeaderboardSubscriber;
  private GameLeaderboardSubscriber diskGameLeaderboardSubscriber;
  private UserLeaderboardSubscriber cloudUserLeaderboardSubscriber;
  private UserLeaderboardSubscriber diskUserLeaderboardSubscriber;
  private FriendsScoreSubscriber diskFriendsScoreSubscriber;
  private LookupContactsSubscriber lookupContactsSubscriber;
  private FriendsScoreSubscriber cloudFriendsScoreSubscriber;

  @Inject public GamePresenter(GetCloudGameLeaderboard cloudGameLeaderboard,
      GetDiskGameLeaderboard diskGameLeaderboard, GetCloudUserLeaderboard cloudUserLeaderboard,
      GetDiskUserLeaderboard diskUserLeaderboard, GetDiskFriendsScores diskFriendsScores,
      GetCloudFriendsScores cloudFriendsScores, GetTriviaData getTriviaData,
      GetBattleMusicData getBattleMusicData, GetCloudGames getGames, GetGamesData getGamesData,
      GetGameFile getGameFile, GetGameData getGameData, GetUserBestScore getUserBestScore,
      RxFacebook rxFacebook,  SynchroContactList synchroContactList) {
    this.cloudGameLeaderboard = cloudGameLeaderboard;
    this.diskGameLeaderboard = diskGameLeaderboard;
    this.cloudUserLeaderboard = cloudUserLeaderboard;
    this.diskUserLeaderboard = diskUserLeaderboard;
    this.diskFriendsScores = diskFriendsScores;
    this.cloudFriendsScores = cloudFriendsScores;
    this.getTriviaData = getTriviaData;
    this.getBattleMusicData = getBattleMusicData;
    this.getGames = getGames;
    this.getGamesData = getGamesData;
    this.getGameFile = getGameFile;
    this.getGameData = getGameData;
    this.getUserBestScore = getUserBestScore;
    this.rxFacebook = rxFacebook;
    this.synchroContactList = synchroContactList;
  }

  @Override public void onViewDetached() {
    cloudGameLeaderboard.unsubscribe();
    diskGameLeaderboard.unsubscribe();
    cloudUserLeaderboard.unsubscribe();
    diskUserLeaderboard.unsubscribe();
    cloudFriendsScores.unsubscribe();
    diskFriendsScores.unsubscribe();
    getTriviaData.unsubscribe();
    getBattleMusicData.unsubscribe();
    getGames.unsubscribe();
    getGameFile.unsubscribe();
    getGameData.unsubscribe();
    gameMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    gameMVPView = (GameMVPView) v;
    gameManager = GameManager.getInstance(gameMVPView.context());
  }

  public void loadGameLeaderboard(String gameId, List<Contact> usersId) {
    if (cloudGameLeaderboardSubscriber != null) {
      cloudGameLeaderboardSubscriber.unsubscribe();
    }

    cloudGameLeaderboardSubscriber = new GameLeaderboardSubscriber(true);
    cloudGameLeaderboard.setup(gameId, usersId);
    cloudGameLeaderboard.execute(cloudGameLeaderboardSubscriber);
  }

  public void lookupContacts() {
    if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
    lookupContactsSubscriber = new LookupContactsSubscriber();
    synchroContactList.execute(lookupContactsSubscriber);
  }

  private class LookupContactsSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
      gameMVPView.onLookupContactsError(e.getMessage());
    }

    @Override public void onNext(List<Contact> contactList) {
      gameMVPView.onLookupContacts(contactList);
    }
  }

  public void loginFacebook() {
    if (!FacebookUtils.isLoggedIn()) {
      rxFacebook.requestLogin().subscribe(loginResult -> {
        if (FacebookUtils.isLoggedIn()) {
          gameMVPView.successFacebookLogin();
        } else {
          gameMVPView.errorFacebookLogin();
        }
      });
    } else {
      gameMVPView.successFacebookLogin();
    }
  }
  private final class GameLeaderboardSubscriber extends DefaultSubscriber<List<Score>> {

    private boolean cloud;

    public GameLeaderboardSubscriber(boolean cloud) {
      this.cloud = cloud;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Score> score) {
      if (gameMVPView != null) {
        gameMVPView.onGameLeaderboard(score, cloud);
        unsubscribe();
      }
    }
  }

  public void loadUserLeaderboard(String userId) {
    if (diskUserLeaderboardSubscriber != null) {
      diskUserLeaderboardSubscriber.unsubscribe();
    }

    diskUserLeaderboardSubscriber = new UserLeaderboardSubscriber(false);
    diskUserLeaderboard.setup(userId);
    diskUserLeaderboard.execute(diskUserLeaderboardSubscriber);

    if (cloudUserLeaderboardSubscriber != null) {
      cloudUserLeaderboardSubscriber.unsubscribe();
    }

    cloudUserLeaderboardSubscriber = new UserLeaderboardSubscriber(true);
    cloudUserLeaderboard.setup(userId);
    cloudUserLeaderboard.execute(cloudUserLeaderboardSubscriber);
  }

  private final class UserLeaderboardSubscriber extends DefaultSubscriber<List<Score>> {

    private boolean cloud;

    public UserLeaderboardSubscriber(boolean cloud) {
      this.cloud = cloud;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Score> score) {
      if (gameMVPView != null) gameMVPView.onUserLeaderboard(score, cloud);
    }
  }

  public void loadFriendsScore(String gameId) {
    if (diskFriendsScoreSubscriber != null) {
      diskFriendsScoreSubscriber.unsubscribe();
    }

    diskFriendsScoreSubscriber = new FriendsScoreSubscriber(false);
    diskFriendsScores.setup(gameId);
    diskFriendsScores.execute(diskFriendsScoreSubscriber);

    if (cloudFriendsScoreSubscriber != null) {
      cloudFriendsScoreSubscriber.unsubscribe();
    }

    cloudFriendsScoreSubscriber = new FriendsScoreSubscriber(true);
    cloudFriendsScores.setup(gameId);
    cloudFriendsScores.execute(cloudFriendsScoreSubscriber);
  }

  private final class FriendsScoreSubscriber extends DefaultSubscriber<List<Score>> {

    private boolean cloud;

    public FriendsScoreSubscriber(boolean cloud) {
      this.cloud = cloud;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Score> score) {
      if (gameMVPView != null) gameMVPView.onFriendsScore(score, cloud);
    }
  }

  public void getTriviaData() {
    getTriviaData.execute(new DefaultSubscriber<Map<String, List<TriviaQuestion>>>() {
      @Override public void onError(Throwable e) {
        Timber.e(e);
      }

      @Override public void onNext(Map<String, List<TriviaQuestion>> map) {
        if (map != null && gameMVPView != null) gameMVPView.onTriviaData(map);
        unsubscribe();
      }
    });
  }

  public void getBattleMusicData() {
    getBattleMusicData.execute(new DefaultSubscriber<Map<String, BattleMusicPlaylist>>() {
      @Override public void onError(Throwable e) {
        Timber.e(e);
      }

      @Override public void onNext(Map<String, BattleMusicPlaylist> map) {
        if (map != null && gameMVPView != null) gameMVPView.onBattleMusicData(map);
        unsubscribe();
      }
    });
  }

  public void getGames() {
    getGames.execute(new DefaultSubscriber<List<Game>>() {
      @Override public void onError(Throwable e) {
        Timber.e(e);
      }

      @Override public void onNext(List<Game> gameList) {
        if (gameMVPView != null) gameMVPView.onGameList(gameList);
      }
    });
  }

  public void synchronizeGameData(String lang, Preference<Long> lastSyncGameData) {
    getGamesData.setup(lang);
    getGamesData.execute(new DefaultSubscriber() {
      @Override public void onNext(Object o) {
        lastSyncGameData.set(System.currentTimeMillis());
        if (getGamesData != null) getGamesData.unsubscribe();
      }
    });
  }

  public void synchronizeGame(String lang, String gameId) {
    getGameData.setup(lang, gameId);
    getGameData.execute(new DefaultSubscriber<List<String>>() {
      @Override public void onNext(List<String> o) {
        if (getGameData != null) getGameData.unsubscribe();
        if (gameMVPView != null) gameMVPView.onGameData(o);
      }
    });
  }

  public void getGameFile(String url) {
    getGameFile.setup(url);
    getGameFile.execute(new DefaultSubscriber<GameFile>() {
      @Override public void onError(Throwable e) {
        Timber.e(e);
      }

      @Override public void onNext(GameFile gameFile) {
        if (gameMVPView != null) gameMVPView.onGameFile(gameFile);
      }
    });
  }

  public void getUserBestScore(String gameId) {
    getUserBestScore.setup(gameId);
    getUserBestScore.execute(new DefaultSubscriber<Score>() {
      @Override public void onNext(Score score) {
        if (gameMVPView != null) gameMVPView.onUserBestScore(score);
      }
    });
  }
}
