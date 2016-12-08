package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.JobStatus;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by tiago on 05/07/2016.
 */
public class UpdateMessagesErrorStatusJob extends BaseJob {

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
        Map<String, List<Pair<String, Object>>> chatUpdates = new HashMap<>();

        for (ChatRealm chatRealm : chatRealmSent) {
            List<Pair<String, Object>> values = new ArrayList<>();

            if (chatRealm.getMessageSendingStatus() != null && chatRealm.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_PENDING) &&
                    jobManager.getJobStatus(chatRealm.getLocalId()).equals(JobStatus.UNKNOWN)) {
                values.add(Pair.create(ChatRealm.MESSAGE_SENDING_STATUS, MessageSendingStatus.STATUS_ERROR));
            }

            chatUpdates.put(chatRealm.getLocalId(), values);
        }

        chatCache.update(chatUpdates);
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
