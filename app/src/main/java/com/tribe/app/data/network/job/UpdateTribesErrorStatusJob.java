package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.JobStatus;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by tiago on 05/07/2016.
 */
public class UpdateTribesErrorStatusJob extends BaseJob {

    @Inject
    User currentUser;

    @Inject
    JobManager jobManager;

    @Inject
    TribeCache tribeCache;

    public UpdateTribesErrorStatusJob() {
        super(new Params(Priority.HIGH).groupBy("update-error-tribe").setSingleId("update-error-tribe"));
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        List<TribeRealm> tribeRealmSent = tribeCache.tribesNotSent();
        List<TribeRealm> tribeRealmSentFiltered = new ArrayList<>();

        for (TribeRealm tribeRealm : tribeRealmSent) {
            if (tribeRealm.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_PENDING) &&
                    jobManager.getJobStatus(tribeRealm.getLocalId()).equals(JobStatus.UNKNOWN)) {
                tribeRealmSentFiltered.add(tribeRealm);
            }
        }

        tribeCache.updateToError(tribeRealmSentFiltered);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }
}
