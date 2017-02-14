package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 11/28/16.
 */
public class DiskGetMembership extends UseCaseDisk {

  private UserRepository userRepository;
  private String membershipId;

  @Inject public DiskGetMembership(DiskUserDataRepository userDataRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.userRepository = userDataRepository;
  }

  public void prepare(String membershipId) {
    this.membershipId = membershipId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.getMembershipInfos(membershipId);
  }
}
