package com.tribe.app.domain.interactor.live;

import com.tribe.app.data.repository.live.CloudLiveDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 08/24/2017.
 */
public class CreateRoom extends UseCase {

  private String name;
  private String gameId;
  private LiveRepository liveRepository;

  @Inject public CreateRoom(CloudLiveDataRepository liveRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.liveRepository = liveRepository;
  }

  public void setup(String name, String gameId) {
    this.name = name;
    this.gameId = gameId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.liveRepository.createRoom(name, gameId);
  }
}
