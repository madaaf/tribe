package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.game.GetBattleMusicData;
import com.tribe.app.domain.interactor.game.GetCloudFriendsScores;
import com.tribe.app.domain.interactor.game.GetCloudGameLeaderboard;
import com.tribe.app.domain.interactor.game.GetCloudGames;
import com.tribe.app.domain.interactor.game.GetCloudUserLeaderboard;
import com.tribe.app.domain.interactor.game.GetDiskFriendsScores;
import com.tribe.app.domain.interactor.game.GetDiskGameLeaderboard;
import com.tribe.app.domain.interactor.game.GetDiskUserLeaderboard;
import com.tribe.app.domain.interactor.game.GetTriviaData;
import com.tribe.app.presentation.mvp.view.GameMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
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

  // SUBSCRIBERS
  private GameLeaderboardSubscriber cloudGameLeaderboardSubscriber;
  private GameLeaderboardSubscriber diskGameLeaderboardSubscriber;
  private UserLeaderboardSubscriber cloudUserLeaderboardSubscriber;
  private UserLeaderboardSubscriber diskUserLeaderboardSubscriber;
  private FriendsScoreSubscriber diskFriendsScoreSubscriber;
  private FriendsScoreSubscriber cloudFriendsScoreSubscriber;

  @Inject public GamePresenter(GetCloudGameLeaderboard cloudGameLeaderboard,
      GetDiskGameLeaderboard diskGameLeaderboard, GetCloudUserLeaderboard cloudUserLeaderboard,
      GetDiskUserLeaderboard diskUserLeaderboard, GetDiskFriendsScores diskFriendsScores,
      GetCloudFriendsScores cloudFriendsScores, GetTriviaData getTriviaData,
      GetBattleMusicData getBattleMusicData, GetCloudGames getGames) {
    this.cloudGameLeaderboard = cloudGameLeaderboard;
    this.diskGameLeaderboard = diskGameLeaderboard;
    this.cloudUserLeaderboard = cloudUserLeaderboard;
    this.diskUserLeaderboard = diskUserLeaderboard;
    this.diskFriendsScores = diskFriendsScores;
    this.cloudFriendsScores = cloudFriendsScores;
    this.getTriviaData = getTriviaData;
    this.getBattleMusicData = getBattleMusicData;
    this.getGames = getGames;
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
    gameMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    gameMVPView = (GameMVPView) v;
    gameManager = GameManager.getInstance(gameMVPView.context());
  }

  public void loadGameLeaderboard(String gameId, boolean friendsOnly, boolean shouldLoadFromDisk,
      int limit, int offset, boolean downwards) {
    if (offset < 0) return;

    if (cloudGameLeaderboardSubscriber != null) {
      cloudGameLeaderboardSubscriber.unsubscribe();
    }

    cloudGameLeaderboardSubscriber =
        new GameLeaderboardSubscriber(true, friendsOnly, offset, downwards);
    cloudGameLeaderboard.setup(gameId, friendsOnly, limit, offset);
    cloudGameLeaderboard.execute(cloudGameLeaderboardSubscriber);
  }

  private final class GameLeaderboardSubscriber extends DefaultSubscriber<List<Score>> {

    private boolean friendsOnly = false;
    private int offset = 0;
    private boolean cloud;
    private boolean downwards;

    public GameLeaderboardSubscriber(boolean cloud, boolean friendsOnly, int offset,
        boolean downwards) {
      this.cloud = cloud;
      this.friendsOnly = friendsOnly;
      this.offset = offset;
      this.downwards = downwards;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Score> score) {
      if (gameMVPView != null) {
        gameMVPView.onGameLeaderboard(score, cloud, friendsOnly, offset, downwards);
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
}
