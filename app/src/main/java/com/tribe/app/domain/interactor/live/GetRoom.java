package com.tribe.app.domain.interactor.live;

import com.tribe.app.data.repository.live.CloudLiveDataRepository;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class GetRoom extends UseCase {

  private Live live;
  private LiveRepository liveRepository;

  @Inject public GetRoom(CloudLiveDataRepository liveRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.liveRepository = liveRepository;
  }

  public void setup(Live live) {
    this.live = live;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.liveRepository.getRoom(live);
  }
}
