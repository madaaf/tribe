package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import javax.inject.Inject;

public class ActionPresenter implements Presenter {

    private JobManager jobManager;

    @Inject
    public ActionPresenter(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {
        // Unused
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {
        // Unused
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {

    }

    public void updateScoreLocation() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.LOCATION));
    }

    public void updateScoreCamera() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.CAMERA));
    }

    public void updateScoreRating() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.RATE_APP));
    }

    @Override
    public void attachView(View v) {

    }
}
