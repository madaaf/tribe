package com.tribe.app.domain.interactor.live;

import com.tribe.app.data.repository.live.CloudLiveDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class GetRoomMembers extends UseCase {

  private String roomId;
  private LiveRepository liveRepository;

  @Inject
  public GetRoomMembers(CloudLiveDataRepository liveRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.liveRepository = liveRepository;
  }

  public void setup(String roomId) {
    this.roomId = roomId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.liveRepository.getRoomLight(roomId);
  }
}
