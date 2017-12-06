package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 09/12/2017.
 */
public class CreateShortcut extends UseCase {

  private String[] userIds;
  private UserRepository userRepository;
  private User user;

  @Inject
  public CreateShortcut(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread, User user) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
    this.user = user;
  }

  public void setup(String... userIds) {
    this.userIds = userIds;
  }

  @Override protected Observable buildUseCaseObservable() {
    return userRepository.createShortcut(userIds);
  }
}
