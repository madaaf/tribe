package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.domain.interactor.tribe.SendTribe;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.view.widget.CameraWrapper;

public abstract class SendTribePresenter implements Presenter {

    protected JobManager jobManager;
    protected SaveTribe diskSaveTribeUsecase;
    protected DeleteTribe diskDeleteTribeUsecase;
    protected SendTribe cloudSendTribe;

    public SendTribePresenter(JobManager jobManager,
                              SaveTribe diskSaveTribe,
                              DeleteTribe diskDeleteTribe,
                              SendTribe cloudSendTribe) {
        this.jobManager = jobManager;
        this.diskSaveTribeUsecase = diskSaveTribe;
        this.diskDeleteTribeUsecase = diskDeleteTribe;
        this.cloudSendTribe = cloudSendTribe;
    }

    @Override
    public void onDestroy() {
        diskDeleteTribeUsecase.unsubscribe();
        diskSaveTribeUsecase.unsubscribe();
    }

    public String createTribe(User user, Friendship friendship, @CameraWrapper.TribeMode String tribeMode) {
        Tribe tribe = Tribe.createTribe(user, friendship, tribeMode);
        TribeCreateSubscriber subscriber = new TribeCreateSubscriber();
        diskSaveTribeUsecase.setTribe(tribe);
        diskSaveTribeUsecase.execute(subscriber);
        return tribe.getLocalId();
    }

    public String deleteTribe(Tribe tribe) {
        TribeDeleteSubscriber subscriber = new TribeDeleteSubscriber();
        diskDeleteTribeUsecase.setTribe(tribe);
        diskDeleteTribeUsecase.execute(subscriber);
        return tribe.getLocalId();
    }

    public void sendTribe(Tribe tribe) {
        TribeSendSubscriber subscriber = new TribeSendSubscriber();
        cloudSendTribe.setTribe(tribe);
        cloudSendTribe.execute(subscriber);
    }

    private void setCurrentTribe(Tribe tribe) {
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

    protected final class TribeCreateSubscriber extends DefaultSubscriber<Tribe> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Tribe tribe) {
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

    private final class TribeSendSubscriber extends DefaultSubscriber<Tribe> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Tribe tribe) {
            System.out.println("AIE AIE AIE AIE : " + tribe);
        }
    }
}
