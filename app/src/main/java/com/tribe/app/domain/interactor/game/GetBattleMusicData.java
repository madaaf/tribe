package com.tribe.app.domain.interactor.game;

import com.tribe.app.data.repository.game.CloudGameDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 01/18/2017.
 */
public class GetBattleMusicData extends UseCase {

  private GameRepository gameRepository;

  @Inject
  public GetBattleMusicData(CloudGameDataRepository gameRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.gameRepository = gameRepository;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.gameRepository.getBattleMusicData();
  }
}
