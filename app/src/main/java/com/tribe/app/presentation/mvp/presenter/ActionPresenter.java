package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import javax.inject.Inject;

public class ActionPresenter implements Presenter {

    private JobManager jobManager;

    @Inject
    public ActionPresenter(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    @Override
    public void onViewDetached() {

    }

    @Override
    public void onViewAttached(MVPView v) {

    }

    public void updateScoreLocation() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.LOCATION, 1));
    }

    public void updateScoreCamera() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.CAMERA, 1));
    }

    public void updateScoreRating() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.RATE_APP, 1));
    }
}
