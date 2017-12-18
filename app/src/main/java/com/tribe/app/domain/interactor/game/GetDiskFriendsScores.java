package com.tribe.app.domain.interactor.game;

import com.tribe.app.data.repository.game.CloudGameDataRepository;
import com.tribe.app.data.repository.game.DiskGameDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 11/20/2017
 */
public class GetDiskFriendsScores extends UseCaseDisk {

  private GameRepository gameRepository;
  private String gameId;

  @Inject public GetDiskFriendsScores(DiskGameDataRepository gameRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.gameRepository = gameRepository;
  }

  public void setup(String gameId) {
    this.gameId = gameId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.gameRepository.getFriendsScores(gameId);
  }
}
