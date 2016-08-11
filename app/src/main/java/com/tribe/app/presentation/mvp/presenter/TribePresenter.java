package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.MarkTribeListAsReadJob;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.DiskMarkTribeListAsRead;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.mvp.view.TribeView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class TribePresenter extends SendTribePresenter implements Presenter {

    private TribeView tribeView;

    // OBSERVABLES
    private DiskMarkTribeListAsRead diskMarkTribeListAsRead;

    @Inject
    public TribePresenter(JobManager jobManager,
                          @Named("diskSaveTribe") SaveTribe diskSaveTribe,
                          @Named("diskDeleteTribe") DeleteTribe diskDeleteTribe,
                          @Named("diskMarkTribeListAsRead") DiskMarkTribeListAsRead diskMarkTribeListAsRead) {
        super(jobManager, diskSaveTribe, diskDeleteTribe);
        this.diskMarkTribeListAsRead = diskMarkTribeListAsRead;
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

    public void markTribeListAsRead(Recipient recipient, List<TribeMessage> tribeList) {
        diskMarkTribeListAsRead.setTribeList(tribeList);
        diskMarkTribeListAsRead.execute(new DefaultSubscriber<>());
        jobManager.addJobInBackground(new MarkTribeListAsReadJob(recipient, tribeList));
    }
}

