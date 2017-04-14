package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import android.util.Pair;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.UpdateFriendship;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by tiago on 04/10/2017.
 */
public class UnhideFriendshipJob extends BaseJob {

  private static final String TAG = "UpdateFriendshipJob";

  @Inject UpdateFriendship updateFriendship;

  @Inject UserCache userCache;

  private FriendshipRealm friendshipRealm;

  public UnhideFriendshipJob(FriendshipRealm friendshipRealm) {
    super(new Params(Priority.HIGH).requireNetwork().singleInstanceBy(TAG).groupBy(TAG));
    this.friendshipRealm = friendshipRealm;
  }

  @Override public void onAdded() {

  }

  @Override public void onRun() throws Throwable {
    friendshipRealm.setStatus(FriendshipRealm.DEFAULT);
    userCache.updateFriendship(friendshipRealm);

    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(FriendshipRealm.STATUS, FriendshipRealm.DEFAULT));

    if (values.size() > 0) {
      updateFriendship.prepare(friendshipRealm.getId(), values);
      updateFriendship.execute(new DefaultSubscriber());
    }
  }

  @Override protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
    throwable.printStackTrace();
  }

  @Override protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
      int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }

  @Override public void inject(ApplicationComponent appComponent) {
    super.inject(appComponent);
    appComponent.inject(this);
  }
}
