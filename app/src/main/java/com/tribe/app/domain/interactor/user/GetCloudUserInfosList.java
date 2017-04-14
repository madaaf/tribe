package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by madaaflak on 12/04/2017.
 */

public class GetCloudUserInfosList extends UseCase {

  private List<String> userIdsList;
  private UserRepository userRepository;

  @Inject protected GetCloudUserInfosList(CloudUserDataRepository userRepository,
      ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setUserIdsList(List<String> userIdsList) {
    this.userIdsList = userIdsList;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.getUsersInfosList(userIdsList);
  }
}
