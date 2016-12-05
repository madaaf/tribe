package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.SendTribeJob;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.ConfirmTribe;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.widget.CameraWrapper;

public abstract class SendTribePresenter implements Presenter {

    protected JobManager jobManager;
    protected JobManager jobManagerDownload;
    protected SaveTribe diskSaveTribeUsecase;
    protected DeleteTribe diskDeleteTribeUsecase;
    protected ConfirmTribe diskConfirmTribeUsecase;

    private TribeCreateSubscriber tribeCreateSubscriber;
    private TribeDeleteSubscriber tribeDeleteSubscriber;
    private TribeConfirmSubscriber tribeConfirmSubscriber;

    public SendTribePresenter(JobManager jobManager,
                              JobManager jobManagerDownload,
                              SaveTribe diskSaveTribe,
                              DeleteTribe diskDeleteTribe,
                              ConfirmTribe diskConfirmTribe) {
        this.jobManager = jobManager;
        this.jobManagerDownload = jobManagerDownload;
        this.diskSaveTribeUsecase = diskSaveTribe;
        this.diskDeleteTribeUsecase = diskDeleteTribe;
        this.diskConfirmTribeUsecase = diskConfirmTribe;
    }

    @Override
    public void onPause() {
        diskDeleteTribeUsecase.unsubscribe();
        diskSaveTribeUsecase.unsubscribe();
        diskConfirmTribeUsecase.unsubscribe();
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

    public void confirmTribe(String tribeId) {
        if (tribeConfirmSubscriber != null) {
            tribeConfirmSubscriber.unsubscribe();
            diskConfirmTribeUsecase.unsubscribe();
        }

        tribeConfirmSubscriber = new TribeConfirmSubscriber();
        diskConfirmTribeUsecase.setTribeId(tribeId);
        diskConfirmTribeUsecase.execute(tribeConfirmSubscriber);
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

    protected final class TribeConfirmSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {

        }
    }
}
