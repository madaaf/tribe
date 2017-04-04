package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import javax.inject.Inject;

/**
 * Created by tiago on 03/30/2017.
 */
public class RemoveNewStatusContactJob extends BaseJob {

  @Inject ContactCache contactCache;

  public RemoveNewStatusContactJob() {
    super(
        new Params(Priority.HIGH).groupBy("remove-new-status-contact").setSingleId("remove-new-status-contact"));
  }

  @Override public void onAdded() {

  }

  @Override public void onRun() throws Throwable {
    contactCache.removeNewStatus();
  }

  @Override protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

  }

  @Override protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
      int maxRunCount) {
    return null;
  }

  @Override public void inject(ApplicationComponent appComponent) {
    super.inject(appComponent);
    appComponent.inject(this);
  }
}