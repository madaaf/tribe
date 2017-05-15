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
public class GetRequestCode extends UseCase {

  private String phoneNumber;
  private boolean shouldCall;
  private UserRepository userRepository;

  @Inject
  public GetRequestCode(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void prepare(String phoneNumber, boolean shouldCall) {
    this.phoneNumber = phoneNumber;
    this.shouldCall = shouldCall;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.requestCode(phoneNumber, shouldCall);
  }
}
