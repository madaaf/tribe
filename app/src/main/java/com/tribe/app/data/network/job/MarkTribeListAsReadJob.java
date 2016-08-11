package com.tribe.app.data.network.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.CloudMarkTribeListAsRead;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 05/07/2016.
 */
public class MarkTribeListAsReadJob extends BaseJob {

    @Inject
    @Named("cloudMarkTribeListAsRead")
    CloudMarkTribeListAsRead cloudMarkTribeListAsRead;

    // VARIABLES
    private List<TribeMessage> tribeList;

    public MarkTribeListAsReadJob(Recipient recipient, List<TribeMessage> tribeList) {
        super(new Params(Priority.MID).requireNetwork().persist().groupBy(recipient.getId()));
        this.tribeList = tribeList;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        cloudMarkTribeListAsRead.setTribeList(tribeList);
        cloudMarkTribeListAsRead.execute(new MarkTribeListAsReadSubscriber());
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

    private final class MarkTribeListAsReadSubscriber extends DefaultSubscriber<List<TribeMessage>> {

        @Override
        public void onCompleted() {
            cloudMarkTribeListAsRead.unsubscribe();

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(List<TribeMessage> tribeList) {

        }
    }
}
