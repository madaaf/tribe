package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 04/24/2017.
 */
public class GetRoomLink extends UseCase {

  private UserRepository userRepository;

  private String roomId;

  @Inject public GetRoomLink(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setup(String roomId) {
    this.roomId = roomId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.getRoomLink(roomId);
  }
}
