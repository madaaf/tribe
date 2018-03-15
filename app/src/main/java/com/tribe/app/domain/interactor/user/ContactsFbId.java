package com.tribe.app.domain.interactor.user;

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
public class ContactsFbId extends UseCase {
  private Context context;
  private UserRepository userRepository;

  @Inject public ContactsFbId(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setParams(Context context) {
    this.context = context;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.contactsFbId(context);
  }


}
