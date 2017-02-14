package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by horatiothomas on 9/19/16.
 */
public class RemoveMembersFromGroup extends UseCase {

  private UserRepository userRepository;
  private String groupId;
  private List<String> memberIds;

  @Inject RemoveMembersFromGroup(CloudUserDataRepository userDataRepository,
      ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userDataRepository;
  }

  public void prepare(String groupId, List<String> memberIds) {
    this.groupId = groupId;
    this.memberIds = memberIds;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.removeMembersFromGroup(groupId, memberIds);
  }
}
