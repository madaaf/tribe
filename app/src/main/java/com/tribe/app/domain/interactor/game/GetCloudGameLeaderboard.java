package com.tribe.app.domain.interactor.game;

import com.tribe.app.data.repository.game.CloudGameDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 11/20/2017
 */
public class GetCloudGameLeaderboard extends UseCase {

  private GameRepository gameRepository;
  private String gameId;

  @Inject public GetCloudGameLeaderboard(CloudGameDataRepository gameRepository,
      ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.gameRepository = gameRepository;
  }

  public void setup(String gameId) {
    this.gameId = gameId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.gameRepository.getGameLeaderBoard(gameId);
  }
}
