package com.tribe.app.presentation.mvp.presenter;

import android.Manifest;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.JobStatus;
import com.birbit.android.jobqueue.TagConstraint;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.data.network.job.DownloadTribeJob;
import com.tribe.app.data.network.job.SendTribeJob;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;
import com.tribe.app.presentation.view.widget.CameraWrapper;

import java.io.File;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class SendTribePresenter implements Presenter {

    protected JobManager jobManager;
    protected SaveTribe diskSaveTribeUsecase;
    protected DeleteTribe diskDeleteTribeUsecase;

    private TribeCreateSubscriber tribeCreateSubscriber;
    private TribeDeleteSubscriber tribeDeleteSubscriber;

    public SendTribePresenter(JobManager jobManager,
                              SaveTribe diskSaveTribe,
                              DeleteTribe diskDeleteTribe) {
        this.jobManager = jobManager;
        this.diskSaveTribeUsecase = diskSaveTribe;
        this.diskDeleteTribeUsecase = diskDeleteTribe;
    }

    @Override
    public void onPause() {
        diskDeleteTribeUsecase.unsubscribe();
        diskSaveTribeUsecase.unsubscribe();
    }

    @Override
    public void onDestroy() {
        onPause();
    }

    public TribeMessage createTribe(User user, Recipient recipient, @CameraWrapper.TribeMode String tribeMode) {
        TribeMessage tribe = TribeMessage.createTribe(user, recipient, tribeMode);

        if (tribeCreateSubscriber != null) {
            tribeCreateSubscriber.unsubscribe();
            diskSaveTribeUsecase.unsubscribe();
        }

        tribeCreateSubscriber = new TribeCreateSubscriber();

        diskSaveTribeUsecase.setTribe(tribe);
        diskSaveTribeUsecase.execute(tribeCreateSubscriber);
        return tribe;
    }

    public void deleteTribe(TribeMessage... tribeList) {
        for (TribeMessage tribe : tribeList) {
            FileUtils.delete(getView().context(), tribe.getLocalId(), FileUtils.VIDEO);

            if (tribeDeleteSubscriber != null) {
                tribeDeleteSubscriber.unsubscribe();
                diskDeleteTribeUsecase.unsubscribe();
            }

            tribeDeleteSubscriber = new TribeDeleteSubscriber();
            diskDeleteTribeUsecase.setTribe(tribe);
            diskDeleteTribeUsecase.execute(tribeDeleteSubscriber);
        }
    }

    public void sendTribe(TribeMessage... tribeList) {
        for (TribeMessage tribe : tribeList)
            jobManager.addJobInBackground(new SendTribeJob(tribe));
    }

    public void downloadMessages(Message... messageList) {
        if (RxPermissions.getInstance(getView().context()).isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Observable
                    .just("")
                    .doOnNext(o -> {
                        for (Message message : messageList) {
                            if (message instanceof TribeMessage) {
                                boolean shouldDownload = false;

                                JobStatus jobStatus = jobManager.getJobStatus(message.getLocalId());
                                File file = FileUtils.getFile(getView().context(), message.getLocalId(), FileUtils.VIDEO);

                                if (jobStatus.equals(JobStatus.UNKNOWN) && (!file.exists() || file.length() == 0)
                                        && !message.getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_DOWNLOADED)) {
                                    shouldDownload = true;
                                    message.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
                                    jobManager.cancelJobsInBackground(null, TagConstraint.ALL, message.getLocalId());
                                }

                                if (shouldDownload
                                        && message.getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_TO_DOWNLOAD)
                                        && message.getFrom() != null) {
                                    jobManager.addJobInBackground(new DownloadTribeJob((TribeMessage) message));
                                }
                            }
                        }
                    }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    private void setCurrentTribe(TribeMessage tribe) {
        this.getView().setCurrentTribe(tribe);
    }

    protected void showViewLoading() {
        this.getView().showLoading();
    }

    protected void hideViewLoading() {
        this.getView().hideLoading();
    }

    protected void showErrorMessage(ErrorBundle errorBundle) {
        if (this.getView() != null && this.getView().context() != null) {
            String errorMessage = ErrorMessageFactory.create(this.getView().context(),
                    errorBundle.getException());
            this.getView().showError(errorMessage);
        }
    }

    protected abstract SendTribeView getView();

    protected final class TribeCreateSubscriber extends DefaultSubscriber<TribeMessage> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(TribeMessage tribe) {
            setCurrentTribe(tribe);
        }
    }

    protected final class TribeDeleteSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Void aVoid) {

        }
    }
}
