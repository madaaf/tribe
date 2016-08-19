package com.tribe.app.data.network.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.text.CloudMarkMessageListAsRead;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 05/07/2016.
 */
public class MarkMessageListAsReadJob extends BaseJob {

    @Inject
    @Named("cloudMarkMessageListAsRead")
    CloudMarkMessageListAsRead cloudMarkMessageListAsRead;

    // VARIABLES
    private List<ChatMessage> messageList;

    public MarkMessageListAsReadJob(Recipient recipient, List<ChatMessage> messageList) {
        super(new Params(Priority.MID).requireNetwork().persist().groupBy(recipient.getId()));
        this.messageList = messageList;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        cloudMarkMessageListAsRead.setChatList(messageList);
        cloudMarkMessageListAsRead.execute(new MarkMessageListAsReadSubscriber());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    private final class MarkMessageListAsReadSubscriber extends DefaultSubscriber<List<ChatMessage>> {

        @Override
        public void onCompleted() {
            cloudMarkMessageListAsRead.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(List<ChatMessage> messageList) {

        }
    }
}
