package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.CloudUpdateUserListScore;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 10/02/2016.
 */
public class UpdateUserListScoreJob extends BaseJob {

    private static final String TAG = "UpdateUserListScoreJob";

    @Inject
    @Named("cloudUpdateUserListScore")
    CloudUpdateUserListScore cloudUpdateUserListScore;

    private Set<String> userIds;

    public UpdateUserListScoreJob(Set<String> userIds) {
        super(new Params(Priority.MID).requireNetwork().groupBy(TAG));
        this.userIds = userIds;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        cloudUpdateUserListScore.setUserIds(userIds);
        cloudUpdateUserListScore.execute(new DefaultSubscriber<>());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        throwable.printStackTrace();
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

    private final class UpdateUserListScoreSubscriber extends DefaultSubscriber<List<User>> {

        @Override
        public void onCompleted() {
            if (cloudUpdateUserListScore != null) cloudUpdateUserListScore.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(List<User> users) {

        }
    }
}
