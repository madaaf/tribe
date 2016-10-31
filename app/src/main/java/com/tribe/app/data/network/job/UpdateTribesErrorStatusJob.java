package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 05/07/2016.
 */
public class UpdateTribesErrorStatusJob extends BaseJob {

    @Inject
    @Named("userThreadSafe") User currentUser;

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
        Map<String, List<Pair<String, Object>>> tribeUpdates = new HashMap<>();

        for (TribeRealm tribeRealm : tribeRealmSent) {
            List<Pair<String, Object>> values = new ArrayList<>();

            if (tribeRealm.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_PENDING) &&
                    jobManager.getJobStatus(tribeRealm.getLocalId()).equals(JobStatus.UNKNOWN)) {
                values.add(Pair.create(TribeRealm.MESSAGE_SENDING_STATUS, MessageSendingStatus.STATUS_ERROR));
            }

            tribeUpdates.put(tribeRealm.getLocalId(), values);
        }

        tribeCache.update(tribeUpdates);
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
