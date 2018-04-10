package com.tribe.app.domain.interactor.user;

import android.app.Activity;
import android.content.Context;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class GetDiskContactList extends UseCaseDisk {

  private UserRepository userRepository;
  private Context c;

  @Inject public GetDiskContactList(DiskUserDataRepository userRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.userRepository = userRepository;
  }

  private void setParams(Context c){
    this.c = c;
  }
  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.contacts(c);
  }
}
