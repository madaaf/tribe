package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 15/07/2016.
 */
public class UpdateMessagesJob extends BaseJob {

    private static final String TAG = "UpdateMessagesJob";

    @Inject
    @Named("cloudGetMessages")
    UseCase cloudGetMessages;

    public UpdateMessagesJob() {
        super(new Params(Priority.HIGH).requireNetwork().singleInstanceBy(TAG).groupBy(TAG));
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        cloudGetMessages.execute(new TribeListSubscriber());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        //System.out.println("Cancel Reason : " + cancelReason);
        //throwable.printStackTrace();
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        //return RetryConstraint.createExponentialBackoff(runCount, 1000);
        return RetryConstraint.CANCEL;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    private final class TribeListSubscriber extends DefaultSubscriber<List<TribeMessage>> {

        @Override
        public void onCompleted() {
            cloudGetMessages.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(List<TribeMessage> tribes) {
        }
    }
}
