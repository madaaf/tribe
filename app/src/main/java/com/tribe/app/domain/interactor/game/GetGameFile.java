package com.tribe.app.domain.interactor.game;

import com.tribe.app.data.repository.game.DiskGameDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 11/20/2017
 */
public class GetGameFile extends UseCaseDisk {

  private GameRepository gameRepository;
  private String url;

  @Inject public GetGameFile(DiskGameDataRepository gameRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.gameRepository = gameRepository;
  }

  public void setup(String url) {
    this.url = url;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.gameRepository.getGameFile(url);
  }
}
