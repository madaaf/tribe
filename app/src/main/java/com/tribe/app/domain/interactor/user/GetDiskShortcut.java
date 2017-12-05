package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 09/12/2017.
 */
public class GetDiskShortcut extends UseCaseDisk {

  private String shortcutId;
  private UserRepository userRepository;

  @Inject public GetDiskShortcut(DiskUserDataRepository userRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setShortcutId(String shortcutId) {
    this.shortcutId = shortcutId;
  }

  @Override protected Observable buildUseCaseObservable() {
    return userRepository.getShortcuts(shortcutId);
  }
}
