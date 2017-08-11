package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import java.util.Set;

import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 12/19/2016.
 */
public class SearchLocally extends UseCaseDisk {

  private UserRepository userRepository;
  private String constraint;
  private Set<String> includedUserIds;

  @Inject public SearchLocally(DiskUserDataRepository userRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setup(String constraint, Set<String> includedUserIds) {
    this.constraint = constraint;
    this.includedUserIds = includedUserIds;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.searchLocally(constraint, includedUserIds);
  }
}
