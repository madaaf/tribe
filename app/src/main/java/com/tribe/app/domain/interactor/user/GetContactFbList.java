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
public class GetContactFbList extends UseCase {

  private UserRepository userRepository;
  private int number;
  private Context c;

  @Inject
  public GetContactFbList(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setParams(int number, Context c) {
    this.number = number;
    this.c = c;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.requestInvitableFriends(c, number);
  }
}
