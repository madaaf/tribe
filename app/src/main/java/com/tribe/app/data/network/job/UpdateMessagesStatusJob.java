package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import javax.inject.Inject;

/**
 * Created by tiago on 05/07/2016.
 */
public class UpdateMessagesStatusJob extends BaseJob {

    @Inject
    User currentUser;

    @Inject
    JobManager jobManager;

    @Inject
    ChatCache chatCache;

    // VARIABLES
    private String recipientId;

    public UpdateMessagesStatusJob(String recipientId) {
        super(new Params(Priority.HIGH).groupBy("update-message-status").setSingleId("update-message-status"));
        this.recipientId = recipientId;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        //chatCache.update(chatRealmSentFiltered);
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
        //appComponent.inject(this);
    }
}
