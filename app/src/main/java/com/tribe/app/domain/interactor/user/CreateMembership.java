package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class CreateMembership extends UseCase {

  private String groupId;
  private UserRepository userRepository;

  @Inject
  public CreateMembership(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.createMembership(groupId);
  }
}
