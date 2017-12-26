package com.tribe.app.domain.interactor.game;

import com.tribe.app.data.repository.game.DiskGameDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 11/20/2017
 */
public class GetDiskGames extends UseCaseDisk {

  private GameRepository gameRepository;

  @Inject public GetDiskGames(DiskGameDataRepository gameRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.gameRepository = gameRepository;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.gameRepository.getGames();
  }
}
