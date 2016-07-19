package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.mvp.view.TribeView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;
import javax.inject.Named;

public class TribePresenter extends SendTribePresenter implements Presenter {

    private Tribe tribe;
    private TribeView tribeView;

    @Inject
    public TribePresenter(JobManager jobManager,
                          @Named("diskSaveTribe") SaveTribe diskSaveTribe,
                          @Named("diskDeleteTribe") DeleteTribe diskDeleteTribe) {
        super(jobManager, diskSaveTribe, diskDeleteTribe);
    }

    @Override
    public void onCreate() {
        // Unused
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {
        // Unused
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void attachView(View v) {
        tribeView = (TribeView) v;
    }

    @Override
    protected SendTribeView getView() {
        return tribeView;
    }
}
