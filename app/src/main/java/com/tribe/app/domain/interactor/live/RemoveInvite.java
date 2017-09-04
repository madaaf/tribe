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
public class RemoveInvite extends UseCase {

  private String userId;
  private String roomId;
  private LiveRepository liveRepository;

  @Inject public RemoveInvite(CloudLiveDataRepository liveRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.liveRepository = liveRepository;
  }

  public void setup(String roomId, String userId) {
    this.roomId = roomId;
    this.userId = userId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.liveRepository.removeInvite(roomId, userId);
  }
}
