package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class HomeGridPresenter implements Presenter {

    private UseCase cloudUserInfosUsecase;
    private UseCase diskUserInfosUsecase;
    private boolean isTheFriendRequestRunning;

    private HomeGridView homeGridView;

    @Inject
    public HomeGridPresenter(@Named("cloudUserInfos") UseCase cloudUserInfos,
                             @Named("diskUserInfos") UseCase diskUserInfos) {
        this.cloudUserInfosUsecase = cloudUserInfos;
        this.diskUserInfosUsecase = diskUserInfos;
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
        isTheFriendRequestRunning = false;
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
        isTheFriendRequestRunning = true;
        showViewLoading();
        FriendListSubscriber subscriber = new FriendListSubscriber();
        //diskUserInfosUsecase.execute(subscriber);
        cloudUserInfosUsecase.execute(subscriber);
    }

    private void showFriendCollectionInView(List<Friendship> friendList) {
        this.homeGridView.renderFriendshipList(friendList);
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
}
