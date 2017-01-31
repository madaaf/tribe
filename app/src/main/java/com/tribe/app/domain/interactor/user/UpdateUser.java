package com.tribe.app.domain.interactor.user;

import android.util.Pair;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by horatiothomas on 8/31/16.
 */
public class UpdateUser extends UseCase {

  private UserRepository userRepository;
  private List<Pair<String, String>> values;

  @Inject
  protected UpdateUser(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userDataRepository;
  }

  public void prepare(List<Pair<String, String>> values) {
    this.values = values;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.updateUser(values);
  }
}
