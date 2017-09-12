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
 * Created by tiago on 09/12/2017.
 */
public class UpdateShortcut extends UseCase {

  private String shortcutId;
  private List<Pair<String, String>> values;
  private UserRepository userRepository;

  @Inject
  public UpdateShortcut(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
  }

  public void setup(String shortcutId, List<Pair<String, String>> values) {
    this.shortcutId = shortcutId;
    this.values = values;
  }

  @Override protected Observable buildUseCaseObservable() {
    return userRepository.updateShortcut(shortcutId, values);
  }
}
