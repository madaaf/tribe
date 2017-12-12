package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.game.GetCloudGameLeaderboard;
import com.tribe.app.domain.interactor.game.GetCloudUserLeaderboard;
import com.tribe.app.domain.interactor.game.GetDiskGameLeaderboard;
import com.tribe.app.domain.interactor.game.GetDiskUserLeaderboard;
import com.tribe.app.presentation.mvp.view.GameMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import java.util.List;
import javax.inject.Inject;

public class GamePresenter implements Presenter {

  // COMPOSITE PRESENTERS

  // VIEW ATTACHED
  private GameMVPView gameMVPView;

  // USECASES
  private GetCloudGameLeaderboard cloudGameLeaderboard;
  private GetDiskGameLeaderboard diskGameLeaderboard;
  private GetCloudUserLeaderboard cloudUserLeaderboard;
  private GetDiskUserLeaderboard diskUserLeaderboard;

  // SUBSCRIBERS
  private GameLeaderboardSubscriber cloudGameLeaderboardSubscriber;
  private GameLeaderboardSubscriber diskGameLeaderboardSubscriber;
  private UserLeaderboardSubscriber cloudUserLeaderboardSubscriber;
  private UserLeaderboardSubscriber diskUserLeaderboardSubscriber;

  @Inject public GamePresenter(GetCloudGameLeaderboard cloudGameLeaderboard,
      GetDiskGameLeaderboard diskGameLeaderboard, GetCloudUserLeaderboard cloudUserLeaderboard,
      GetDiskUserLeaderboard diskUserLeaderboard) {
    this.cloudGameLeaderboard = cloudGameLeaderboard;
    this.diskGameLeaderboard = diskGameLeaderboard;
    this.cloudUserLeaderboard = cloudUserLeaderboard;
    this.diskUserLeaderboard = diskUserLeaderboard;
  }

  @Override public void onViewDetached() {
    cloudGameLeaderboard.unsubscribe();
    diskGameLeaderboard.unsubscribe();
    cloudUserLeaderboard.unsubscribe();
    diskUserLeaderboard.unsubscribe();
    gameMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    gameMVPView = (GameMVPView) v;
  }

  public void loadGameLeaderboard(String gameId, boolean friendsOnly, boolean shouldLoadFromDisk,
      int limit, int offset, boolean downwards) {
    if (offset < 0) return;
    //if (shouldLoadFromDisk) {
    //  if (diskGameLeaderboardSubscriber != null) {
    //    diskGameLeaderboardSubscriber.unsubscribe();
    //  }
    //
    //  diskGameLeaderboardSubscriber =
    //      new GameLeaderboardSubscriber(false, friendsOnly, offset, downwards);
    //  diskGameLeaderboard.setup(gameId, friendsOnly, limit, offset);
    //  diskGameLeaderboard.execute(diskGameLeaderboardSubscriber);
    //}

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
}
