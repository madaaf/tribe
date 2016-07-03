package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.view.widget.CameraWrapper;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class HomeGridPresenter implements Presenter {

    private UseCase cloudUserInfosUsecase;
    private UseCase diskUserInfosUsecase;
    private UseCase cloudSendTribeUsecase;
    private SaveTribe diskSaveTribeUsecase;

    private HomeGridView homeGridView;

    @Inject
    public HomeGridPresenter(@Named("cloudUserInfos") UseCase cloudUserInfos,
                             @Named("diskUserInfos") UseCase diskUserInfos,
                             @Named("cloudSendTribe") UseCase cloudSendTribe,
                             @Named("diskSaveTribe") SaveTribe diskSaveTribe) {
        this.cloudUserInfosUsecase = cloudUserInfos;
        this.diskUserInfosUsecase = diskUserInfos;
        this.cloudSendTribeUsecase = cloudSendTribe;
        this.diskSaveTribeUsecase = diskSaveTribe;
    }

    @Override
    public void onCreate() {
        loadFriendList();
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
        diskUserInfosUsecase.unsubscribe();
        cloudUserInfosUsecase.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        homeGridView = (HomeGridView) v;
    }

    public void loadFriendList() {
        showViewLoading();
        FriendListSubscriber subscriber = new FriendListSubscriber();
        //diskUserInfosUsecase.execute(subscriber);
        cloudUserInfosUsecase.execute(subscriber);
    }

    public String createTribe(User user, Friendship friendship, @CameraWrapper.TribeMode String tribeMode) {
        Tribe tribe = Tribe.createTribe(user, friendship, tribeMode);
        TribeCreateSubscriber subscriber = new TribeCreateSubscriber();
        diskSaveTribeUsecase.setTribe(tribe);
        diskSaveTribeUsecase.execute(subscriber);
        return tribe.getId();
    }

    private void showFriendCollectionInView(List<Friendship> friendList) {
        this.homeGridView.renderFriendshipList(friendList);
    }

    private void setCurrentTribe(Tribe tribe) {
        this.homeGridView.setCurrentTribe(tribe);
    }

    private void showViewLoading() {
        this.homeGridView.showLoading();
    }

    private void hideViewLoading() {
        this.homeGridView.hideLoading();
    }

    private void showErrorMessage(ErrorBundle errorBundle) {
        String errorMessage = ErrorMessageFactory.create(this.homeGridView.context(),
                errorBundle.getException());
        this.homeGridView.showError(errorMessage);
    }

    private final class FriendListSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(User user) {
            showFriendCollectionInView(user.getFriendshipList());
        }
    }

    private final class TribeCreateSubscriber extends DefaultSubscriber<Tribe> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(Tribe tribe) {
            setCurrentTribe(tribe);
        }
    }
}
