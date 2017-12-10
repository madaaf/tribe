package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.game.GetCloudGameLeaderboard;
import com.tribe.app.domain.interactor.game.GetDiskGameLeaderboard;
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

  // SUBSCRIBERS
  private LeaderboardSubscriber cloudLeaderboardSubscriber;
  private LeaderboardSubscriber diskLeaderboardSubscriber;

  @Inject public GamePresenter(GetCloudGameLeaderboard cloudGameLeaderboard,
      GetDiskGameLeaderboard diskGameLeaderboard) {
    this.cloudGameLeaderboard = cloudGameLeaderboard;
    this.diskGameLeaderboard = diskGameLeaderboard;
  }

  @Override public void onViewDetached() {
    cloudGameLeaderboard.unsubscribe();
    gameMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    gameMVPView = (GameMVPView) v;
  }

  public void loadGameLeaderboard(String gameId, boolean friendsOnly, int offset) {
    if (offset == 0) {
      if (diskLeaderboardSubscriber != null) {
        diskLeaderboardSubscriber.unsubscribe();
      }

      diskLeaderboardSubscriber = new LeaderboardSubscriber(false, friendsOnly, offset);
      diskGameLeaderboard.setup(gameId, friendsOnly, offset);
      diskGameLeaderboard.execute(diskLeaderboardSubscriber);
    }

    if (cloudLeaderboardSubscriber != null) {
      cloudLeaderboardSubscriber.unsubscribe();
    }

    cloudLeaderboardSubscriber = new LeaderboardSubscriber(true, friendsOnly, offset);
    cloudGameLeaderboard.setup(gameId, friendsOnly, offset);
    cloudGameLeaderboard.execute(cloudLeaderboardSubscriber);
  }

  private final class LeaderboardSubscriber extends DefaultSubscriber<List<Score>> {

    private boolean friendsOnly = false;
    private int offset = 0;
    private boolean cloud;

    public LeaderboardSubscriber(boolean cloud, boolean friendsOnly, int offset) {
      this.cloud = cloud;
      this.friendsOnly = friendsOnly;
      this.offset = offset;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Score> score) {
      if (gameMVPView != null) gameMVPView.onGameLeaderboard(score, cloud, friendsOnly, offset);
    }
  }
}
