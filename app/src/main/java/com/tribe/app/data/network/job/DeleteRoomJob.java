package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.live.DeleteRoom;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.StringUtils;
import javax.inject.Inject;

/**
 * Created by tiago on 09/22/2017.
 */
public class DeleteRoomJob extends BaseJob {

  private static final String TAG = "DeleteRoomJob";

  @Inject DeleteRoom deleteRoom;

  private String roomId;

  public DeleteRoomJob(String roomId) {
    super(new Params(Priority.HIGH).requireNetwork().singleInstanceBy(TAG).groupBy(TAG));
    this.roomId = roomId;
  }

  @Override public void onAdded() {

  }

  @Override public void onRun() throws Throwable {
    if (StringUtils.isEmpty(roomId)) return;
    deleteRoom.setup(roomId);
    deleteRoom.execute(new DefaultSubscriber());
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
