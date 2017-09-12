package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 09/12/2017.
 */
public class RemoveShortcut extends UseCase {

  private String shortcutId;
  private UserRepository userRepository;

  @Inject
  public RemoveShortcut(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setup(String shortcutId) {
    this.shortcutId = shortcutId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return userRepository.removeShortcut(shortcutId);
  }
}
