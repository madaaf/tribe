package com.tribe.app.domain.interactor.live;

import com.tribe.app.data.repository.live.CloudLiveDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by tiago on 04/05/2016.
 */
public class CreateInvite extends UseCase {

  private String[] userIds;
  private String roomId;
  private LiveRepository liveRepository;

  @Inject public CreateInvite(CloudLiveDataRepository liveRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.liveRepository = liveRepository;
  }

  public void setup(String roomId, String... userIds) {
    this.roomId = roomId;
    this.userIds = userIds;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.liveRepository.createInvite(roomId, userIds);
  }
}
