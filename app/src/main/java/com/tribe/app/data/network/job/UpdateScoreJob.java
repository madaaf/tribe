package com.tribe.app.data.network.job;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.tribe.app.R;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tiago on 15/07/2016.
 */
public class UpdateScoreJob extends BaseJob {

    private static final String TAG = "UpdateScoreJob";

    @Inject
    User currentUser;

    @Inject
    TribeApi tribeApi;

    @Inject
    UserCache userCache;

    private ScoreUtils.Point point;

    public UpdateScoreJob(ScoreUtils.Point point) {
        super(new Params(Priority.MID).requireNetwork().groupBy(TAG));
        this.point = point;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        tribeApi.updateScore(getApplicationContext().getString(R.string.user_mutate_score, point.getServerKey(),
                getApplicationContext().getString(R.string.userfragment_infos)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userRealm -> userCache.put(userRealm));
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
}
