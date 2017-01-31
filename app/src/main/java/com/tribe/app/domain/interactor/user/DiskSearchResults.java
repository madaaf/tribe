package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class DiskSearchResults extends UseCaseDisk {

  private String username;
  private UserRepository userRepository;

  @Inject public DiskSearchResults(DiskUserDataRepository userRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.findByUsername(username);
  }
}
