package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.SendTribeJob;
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
import com.tribe.app.presentation.view.widget.CameraWrapper;

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
    public void onDestroy() {
        diskDeleteTribeUsecase.unsubscribe();
        diskSaveTribeUsecase.unsubscribe();
    }

    public TribeMessage createTribe(User user, Recipient recipient, @CameraWrapper.TribeMode String tribeMode) {
        TribeMessage tribe = TribeMessage.createTribe(user, recipient, tribeMode);

        if (tribeCreateSubscriber == null) {
            tribeCreateSubscriber = new TribeCreateSubscriber();
        }

        diskSaveTribeUsecase.setTribe(tribe);
        diskSaveTribeUsecase.execute(tribeCreateSubscriber);
        return tribe;
    }

    public void deleteTribe(TribeMessage... tribeList) {
        for (TribeMessage tribe : tribeList) {
            FileUtils.delete(tribe.getLocalId(), FileUtils.VIDEO);

            if (tribeDeleteSubscriber == null) {
                tribeDeleteSubscriber = new TribeDeleteSubscriber();
            }

            diskDeleteTribeUsecase.setTribe(tribe);
            diskDeleteTribeUsecase.execute(tribeDeleteSubscriber);
        }
    }

    public void sendTribe(TribeMessage... tribeList) {
        for (TribeMessage tribe : tribeList)
            jobManager.addJobInBackground(new SendTribeJob(tribe));
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
        String errorMessage = ErrorMessageFactory.create(this.getView().context(),
                errorBundle.getException());
        this.getView().showError(errorMessage);
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
