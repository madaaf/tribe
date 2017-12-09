package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.game.GetCloudGameLeaderboard;
import com.tribe.app.presentation.mvp.view.GameMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class GamePresenter implements Presenter {

  // COMPOSITE PRESENTERS

  // VIEW ATTACHED
  private GameMVPView gameMVPView;

  // USECASES
  private GetCloudGameLeaderboard cloudGameLeaderboard;

  // SUBSCRIBERS

  @Inject public GamePresenter(GetCloudGameLeaderboard cloudGameLeaderboard) {
    this.cloudGameLeaderboard = cloudGameLeaderboard;
  }

  @Override public void onViewDetached() {
    cloudGameLeaderboard.unsubscribe();
    gameMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    gameMVPView = (GameMVPView) v;
  }

  public void loadGameLeaderboard(String gameId, boolean friendsOnly, int offset) {
    cloudGameLeaderboard.setup(gameId, friendsOnly, offset);
    cloudGameLeaderboard.execute(new DefaultSubscriber<List<Score>>() {
      @Override public void onNext(List<Score> scoreList) {
        Timber.d("loadGameLeaderboard : " + scoreList);
      }
    });
  }
}
