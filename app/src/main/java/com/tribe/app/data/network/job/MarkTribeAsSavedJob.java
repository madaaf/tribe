package com.tribe.app.data.network.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.CloudMarkTribeAsSave;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tiago on 05/07/2016.
 */
public class MarkTribeAsSavedJob extends BaseJob {

    @Inject
    @Named("cloudMarkTribeAsSave")
    CloudMarkTribeAsSave cloudMarkTribeAsSave;

    // VARIABLES
    private TribeMessage tribe;

    public MarkTribeAsSavedJob(Recipient recipient, TribeMessage tribe) {
        super(new Params(Priority.MID).requireNetwork().persist().groupBy(recipient.getSubId()));
        this.tribe = tribe;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        cloudMarkTribeAsSave.setTribe(tribe);
        cloudMarkTribeAsSave.execute(new DefaultSubscriber<>());
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
}
