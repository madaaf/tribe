package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by Mada on 30/05/2017.
 */
public class ReportUser extends UseCase {

  private UserRepository userRepository;

  private String userId;

  @Inject public ReportUser(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.reportUser(userId);
  }
}
