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
 * Created by tiago on 11/28/16.
 */
public class UpdateGroup extends UseCase {

  private UserRepository userRepository;
  private String groupId;
  private List<Pair<String, String>> values;

  @Inject UpdateGroup(CloudUserDataRepository userDataRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userDataRepository;
  }

  public void prepare(String groupId, List<Pair<String, String>> values) {
    this.groupId = groupId;
    this.values = values;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.updateGroup(groupId, values);
  }
}
