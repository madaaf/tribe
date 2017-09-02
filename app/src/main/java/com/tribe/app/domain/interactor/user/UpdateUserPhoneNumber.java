package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by remy on 07/08/2017.
 */

public class UpdateUserPhoneNumber extends UseCase {

  private UserRepository userRepository;
  private String userId;
  private String accessToken;
  private String phoneNumber;

  @Inject protected UpdateUserPhoneNumber(CloudUserDataRepository userDataRepository,
      ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userDataRepository;
  }

  public void prepare(String userId, String accessToken, String phoneNumber) {
    this.userId = userId;
    this.accessToken = accessToken;
    this.phoneNumber = phoneNumber;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.updateUserPhoneNumber(userId, accessToken, phoneNumber);
  }
}
