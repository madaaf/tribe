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
 * Created by tiago on 11/29/16.
 */
public class UpdateFriendship extends UseCase {

  private UserRepository userRepository;
  private String friendshipId;
  private List<Pair<String, String>> values;

  @Inject UpdateFriendship(CloudUserDataRepository userDataRepository,
      ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.userRepository = userDataRepository;
  }

  public void prepare(String membershipId, List<Pair<String, String>> values) {
    this.friendshipId = membershipId;
    this.values = values;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.userRepository.updateFriendship(friendshipId, values);
  }
}