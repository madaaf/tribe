package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 15/07/2016.
 */
public class UpdateUserJob extends BaseJob {

    private static final String TAG = "UpdateUserJob";

    @Inject
    @Named("cloudUserInfos")
    UseCase cloudUserInfos;


    public UpdateUserJob() {
        super(new Params(Priority.HIGH).requireNetwork().singleInstanceBy(TAG).groupBy(TAG));
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        cloudUserInfos.execute(new FriendListSubscriber());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        System.out.println("Cancel Reason : " + cancelReason);
        throwable.printStackTrace();
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        System.out.println("Cancel Reason : " + throwable.getMessage());
        return RetryConstraint.RETRY;
    }

    @Override
    public void inject(ApplicationComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    private final class FriendListSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {
            if (cloudUserInfos != null) cloudUserInfos.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(User user) {
        }
    }
}
