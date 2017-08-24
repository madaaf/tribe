package com.tribe.app.domain.interactor.live;

import com.tribe.app.data.repository.live.DiskLiveDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.UserRepository;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by Mada on 30/05/2017.
 */
public class RandomRoomAssigned extends UseCase {

  private LiveRepository liveRepository;

  @Inject
  public RandomRoomAssigned(DiskLiveDataRepository liveRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.liveRepository = liveRepository;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.liveRepository.randomRoomAssigned();
  }
}
