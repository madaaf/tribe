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
public class InviteUserToRoom extends UseCase {

  private String userId;
  private String roomId;
  private UserRepository userRepository;

  @Inject
  public InviteUserToRoom(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setup(String roomId, String userId) {
    this.roomId = roomId;
    this.userId = userId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.inviteUserToRoom(roomId, userId);
  }
}
