package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 03/10/2016.
 */
public class GetHeadDeepLink extends UseCase {

  private String link;
  private UserRepository userRepository;

  @Inject
  public GetHeadDeepLink(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void prepare(String link) {
    this.link = link;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.getHeadDeepLink(link);
  }
}
