package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.live.CloudLiveDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.live.LiveRepository;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class RemoveMessage extends UseCase {

  private String messageId;
  private LiveRepository liveRepository;

  @Inject
  public RemoveMessage(CloudLiveDataRepository liveRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.liveRepository = liveRepository;
  }

  public void setup(String messageId) {
    this.messageId = messageId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.liveRepository.removeMessage(messageId);
  }
}
