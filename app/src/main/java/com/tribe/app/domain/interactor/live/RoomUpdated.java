package com.tribe.app.domain.interactor.live;

import com.tribe.app.data.repository.live.DiskLiveDataRepository;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.UserRepository;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 08/30/2017.
 */
public class RoomUpdated extends UseCase {

  private LiveRepository liveRepository;

  @Inject public RoomUpdated(DiskLiveDataRepository liveRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.liveRepository = liveRepository;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.liveRepository.getRoomUpdated();
  }
}
