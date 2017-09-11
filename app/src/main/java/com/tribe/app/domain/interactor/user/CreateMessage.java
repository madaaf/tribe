package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

public class CreateMessage extends UseCase {

  private String[] userIds;
  private String data;
  private UserRepository userRepository;

  @Inject
  public CreateMessage(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setParams(String[] userIds, String data) {
    this.userIds = userIds;
    this.data = data;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.createMessage(userIds, data);
  }
}
