package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class DoLoginWithPhoneNumber extends UseCase {

  private LoginEntity loginEntity;
  private UserRepository userRepository;

  @Inject public DoLoginWithPhoneNumber(CloudUserDataRepository userRepository,
      ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void prepare(LoginEntity loginEntity) {
    this.loginEntity = loginEntity;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.loginWithPhoneNumber(loginEntity);
  }
}
