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
  private String type;
  private UserRepository userRepository;

  @Inject
  public CreateMessage(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setParams(String[] userIds, String data, String type) {
    this.userIds = userIds;
    this.data = data;
    this.type = type;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.createMessage(userIds, type, data);
  }
}
