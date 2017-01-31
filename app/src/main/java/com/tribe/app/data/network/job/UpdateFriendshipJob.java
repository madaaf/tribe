package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.CloudUpdateFriendship;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 10/02/2016.
 */
public class UpdateFriendshipJob extends BaseJob {

  private static final String TAG = "UpdateFriendshipJob";

  @Inject @Named("cloudUpdateFriendship") transient CloudUpdateFriendship cloudUpdateFriendship;

  private @FriendshipRealm.FriendshipStatus String status;
  private String friendshipId;

  public UpdateFriendshipJob(String friendshipId, @FriendshipRealm.FriendshipStatus String status) {
    super(
        new Params(Priority.MID).requireNetwork().persist().groupBy(TAG).setGroupId(friendshipId));
    this.status = status;
    this.friendshipId = friendshipId;
  }

  @Override public void onAdded() {

  }

  @Override public void onRun() throws Throwable {
    cloudUpdateFriendship.prepare(friendshipId, status);
    cloudUpdateFriendship.execute(new UpdateFriendshipSubscriber());
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

  public final class UpdateFriendshipSubscriber extends DefaultSubscriber<Friendship> {

    @Override public void onCompleted() {
      if (cloudUpdateFriendship != null) cloudUpdateFriendship.unsubscribe();
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Friendship friendship) {
    }
  }
}
