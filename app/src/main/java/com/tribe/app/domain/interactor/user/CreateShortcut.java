package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.CloudUserDataRepository;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.presentation.view.ShortcutUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by tiago on 09/12/2017.
 */
public class CreateShortcut extends UseCase {

  private String[] userIds;
  private UserRepository userRepository;
  private User user;

  @Inject
  public CreateShortcut(CloudUserDataRepository userRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread, User user) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userRepository;
    this.user = user;
  }

  public void setup(String... userIds) {
    this.userIds = userIds;
  }

  @Override protected Observable buildUseCaseObservable() {
    List<String> listId = new ArrayList<>(Arrays.asList(userIds));
    ShortcutUtil.removeMyId(listId, user);
    if (!listId.isEmpty()) {
      return userRepository.createShortcut(userIds);
    } else {
      Timber.e("id list is empty for create shortcut");
      return null;
    }
  }
}
