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
    private int count = 1;

    public UpdateScoreJob(ScoreUtils.Point point, int count) {
        super(new Params(Priority.MID).requireNetwork().groupBy(TAG));
        this.point = point;
        this.count = count;
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < count; i++) {
            buffer.append(getApplicationContext().getString(R.string.user_mutate_score_core, i, point.getServerKey()));
        }

        String scoreMutation = getApplicationContext().getString(R.string.user_mutate_score, buffer.toString());

        tribeApi.updateScore(scoreMutation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(scoreEntity -> userCache.updateScore(currentUser.getId(), scoreEntity.getScore()));
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
