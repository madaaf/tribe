package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.text.CloudManageChatHistory;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 08/31/2016.
 */
public class UpdateChatHistoryJob extends BaseJob {

    private static final String TAG = "UpdateChatHistoryJob";

    @Inject
    @Named("manageChatHistory")
    CloudManageChatHistory manageChatHistory;

    // VARIABLES
    private Recipient recipient;

    public UpdateChatHistoryJob(Recipient recipient) {
        super(new Params(Priority.HIGH).requireNetwork().singleInstanceBy(TAG).groupBy(TAG));
        this.recipient = recipient;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        manageChatHistory.setRecipientId(recipient.getFriendshipId());
        manageChatHistory.execute(new UpdateChatHistorySubscriber());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    private final class UpdateChatHistorySubscriber extends DefaultSubscriber<List<ChatMessage>> {

        @Override
        public void onCompleted() {
            manageChatHistory.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(List<ChatMessage> chatMessageList) {
        }
    }
}
