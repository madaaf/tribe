package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.MarkTribeAsSavedJob;
import com.tribe.app.data.network.job.MarkTribeListAsReadJob;
import com.tribe.app.data.network.job.UpdateTribeToDownloadJob;
import com.tribe.app.data.network.job.UpdateUserListScoreJob;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.tribe.ConfirmTribe;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.GetReceivedDiskTribeList;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.mvp.view.TribeView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

public class TribePresenter extends SendTribePresenter implements Presenter {

    private TribeView tribeView;

    // OBSERVABLES
    private GetReceivedDiskTribeList diskGetReceivedTribeList;

    @Inject
    public TribePresenter(JobManager jobManager,
                          @Named("jobManagerDownload") JobManager jobManagerDownload,
                          @Named("diskGetReceivedTribeList") UseCaseDisk diskGetReceivedTribeList,
                          @Named("diskSaveTribe") SaveTribe diskSaveTribe,
                          @Named("diskDeleteTribe") DeleteTribe diskDeleteTribe,
                          @Named("diskConfirmTribe") ConfirmTribe diskConfirmTribe) {
        super(jobManager, jobManagerDownload, diskSaveTribe, diskDeleteTribe, diskConfirmTribe);
        this.diskGetReceivedTribeList = (GetReceivedDiskTribeList) diskGetReceivedTribeList;
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
        diskGetReceivedTribeList.unsubscribe();
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

    public void loadTribes(String recipientId) {
        diskGetReceivedTribeList.setRecipientId(recipientId);
        diskGetReceivedTribeList.execute(new TribeListSubscriber());
    }

    public void markTribeListAsRead(Recipient recipient, List<TribeMessage> tribeList) {
        jobManager.addJobInBackground(new MarkTribeListAsReadJob(recipient, tribeList));
    }

    public void markTribeAsSave(Recipient recipient, TribeMessage tribeMessage) {
        jobManager.addJobInBackground(new MarkTribeAsSavedJob(recipient, tribeMessage));
    }

    public void updateUserListScore(Set<String> userIds) {
        jobManager.addJobInBackground(new UpdateUserListScoreJob(userIds));
    }

    public void updateTribeToDownload(String tribeId) {
        jobManager.addJobInBackground(new UpdateTribeToDownloadJob(tribeId));
    }

    private final class TribeListSubscriber extends DefaultSubscriber<List<TribeMessage>> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(List<TribeMessage> tribeList) {
            tribeView.updateNewTribes(tribeList);
        }
    }
}

