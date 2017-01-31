package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 09/14/2016.
 */
public class RefreshHowManyFriendsJob extends BaseJob {

    private static final String TAG = "RefreshHowManyFriendsJob";

    @Inject
    @Named("refreshHowManyFriends")
    UseCase refreshHowManyFriends;

    public RefreshHowManyFriendsJob() {
        super(new Params(Priority.LOW).requireNetwork().singleInstanceBy(TAG).groupBy(TAG));
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        refreshHowManyFriends.execute(new HowManyFriendsSubscriber());
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        throwable.printStackTrace();
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

    private final class HowManyFriendsSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {
        }
    }
}
