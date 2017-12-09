package com.tribe.app.domain.interactor.game;

import com.tribe.app.data.repository.game.CloudGameDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 11/20/2017
 */
public class GetDiskGameLeaderboard extends UseCaseDisk {

  private GameRepository gameRepository;
  private String gameId;
  private boolean friendsOnly = true;

  @Inject public GetDiskGameLeaderboard(CloudGameDataRepository gameRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.gameRepository = gameRepository;
  }

  public void setup(String gameId, boolean friendsOnly) {
    this.gameId = gameId;
    this.friendsOnly = friendsOnly;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.gameRepository.getGames();
  }
}
