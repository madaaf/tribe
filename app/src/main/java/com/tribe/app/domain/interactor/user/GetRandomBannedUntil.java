package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 09/12/2017.
 */
public class GetRandomBannedUntil extends UseCaseDisk {

  private UserRepository userRepository;

  @Inject public GetRandomBannedUntil(DiskUserDataRepository userRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.userRepository = userRepository;
  }

  @Override protected Observable buildUseCaseObservable() {
    return userRepository.getRandomBannedUntil();
  }
}
