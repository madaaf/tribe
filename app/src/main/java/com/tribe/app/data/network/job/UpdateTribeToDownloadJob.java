package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;

import javax.inject.Inject;

/**
 * Created by tiago on 05/07/2016.
 */
public class UpdateTribeToDownloadJob extends BaseJob {

    @Inject
    TribeCache tribeCache;

    private String tribeId;

    public UpdateTribeToDownloadJob(String tribeId) {
        super(new Params(Priority.HIGH).groupBy("update-tribe-to-download").setSingleId(tribeId).singleInstanceBy(tribeId));
        this.tribeId = tribeId;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        tribeCache.refactorTribeError(tribeId);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

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
}
