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
public class LookupUsername extends UseCase {

  private String username;
  private UserRepository userRepository;

  @Inject
  public LookupUsername(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.lookupUsername(username);
  }
}
