package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.text.CloudUpdateStatuses;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 15/07/2016.
 */
public class UpdateChatMessagesJob extends BaseJob {

    private static final String TAG = "UpdateChatMessagesJob";

    @Inject
    @Named("updateStatuses")
    CloudUpdateStatuses updateStatuses;

    // VARIABLES
    private String recipientId;

    public UpdateChatMessagesJob(String recipientId) {
        super(new Params(Priority.HIGH).requireNetwork().singleInstanceBy(TAG).groupBy(TAG));
        this.recipientId = recipientId;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        updateStatuses.setRecipientId(recipientId);
        updateStatuses.execute(new UpdateStatusesSubscriber());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        //System.out.println("Cancel Reason : " + cancelReason);
        //throwable.printStackTrace();
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    private final class UpdateStatusesSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {
            updateStatuses.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void statuses) {
        }
    }
}
