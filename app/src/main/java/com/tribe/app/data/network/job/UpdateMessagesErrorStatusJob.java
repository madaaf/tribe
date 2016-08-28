package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.JobStatus;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by tiago on 05/07/2016.
 */
public class UpdateMessagesErrorStatusJob extends BaseJob {

    @Inject
    User currentUser;

    @Inject
    JobManager jobManager;

    @Inject
    ChatCache chatCache;

    // VARIABLES
    private String recipientId;

    public UpdateMessagesErrorStatusJob(String recipientId) {
        super(new Params(Priority.HIGH).groupBy("update-error-message").setSingleId("update-error-message"));
        this.recipientId = recipientId;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        List<ChatRealm> chatRealmSent = chatCache.messagesPending(recipientId);
        List<ChatRealm> chatRealmSentFiltered = new ArrayList<>();

        for (ChatRealm chatRealm : chatRealmSent) {
            if (chatRealm.getMessageSendingStatus() != null && chatRealm.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_PENDING) &&
                    jobManager.getJobStatus(chatRealm.getLocalId()).equals(JobStatus.UNKNOWN)) {
                chatRealmSentFiltered.add(chatRealm);
            }
        }

        chatCache.updateToError(chatRealmSentFiltered);
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
