package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by horatiothomas on 9/17/16.
 */
public class CreateGroup extends UseCase {

  private UserRepository userRepository;
  private GroupEntity groupEntity;

  @Inject CreateGroup(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userDataRepository;
  }

  public void prepare(GroupEntity groupEntity) {
    this.groupEntity = groupEntity;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.createGroup(groupEntity);
  }
}
