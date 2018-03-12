package com.tribe.app.domain.interactor.game;

import com.tribe.app.data.repository.game.CloudGameDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class GetGameData extends UseCase {

  private String lang;
  private String gameId;
  private GameRepository gameRepository;

  @Inject public GetGameData(CloudGameDataRepository gameRepository,
      ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.gameRepository = gameRepository;
  }

  public void setup(String lang, String gameId) {
    this.lang = lang;
    this.gameId = gameId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.gameRepository.synchronizeGameData(lang, gameId);
  }
}
