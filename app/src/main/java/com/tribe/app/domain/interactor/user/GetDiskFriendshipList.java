package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 01/18/2017.
 */
public class GetDiskFriendshipList extends UseCaseDisk {

  private UserRepository userRepository;

  @Inject public GetDiskFriendshipList(DiskUserDataRepository userRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.userRepository = userRepository;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.friendships();
  }
}
