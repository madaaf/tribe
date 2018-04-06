package com.tribe.app.domain.interactor.user;

import android.app.Activity;
import android.content.Context;
import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class SynchroContactList extends UseCase {

  private UserRepository userRepository;
  private Activity c;

  @Inject
  public SynchroContactList(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setParams(Activity c) {
    this.c = c;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.contacts(c);
  }
}
