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
public class JoinRoom extends UseCase {

  private String id;
  private boolean isGroup;
  private UserRepository userRepository;

  @Inject public JoinRoom(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setup(String id, boolean isGroup) {
    this.id = id;
    this.isGroup = isGroup;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.joinRoom(id, isGroup);
  }
}
