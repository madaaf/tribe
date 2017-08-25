package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class CreateFriendship extends UseCase {

  private String userId;
  private long debounceDelay = 0;
  private UserRepository userRepository;

  @Inject
  public CreateFriendship(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void configure(String userId, long debounceDelay) {
    this.userId = userId;
    this.debounceDelay = debounceDelay;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.createFriendship(userId).debounce(debounceDelay, TimeUnit.MILLISECONDS);
  }
}
