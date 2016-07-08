package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.domain.interactor.tribe.SendTribe;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class HomeGridPresenter extends SendTribePresenter implements Presenter {

    private UseCase cloudUserInfosUsecase;
    private UseCase diskUserInfosUsecase;

    private HomeGridView homeGridView;

    @Inject
    public HomeGridPresenter(JobManager jobManager,
                             @Named("cloudUserInfos") UseCase cloudUserInfos,
                             @Named("diskUserInfos") UseCase diskUserInfos,
                             @Named("diskSaveTribe") SaveTribe diskSaveTribe,
                             @Named("diskDeleteTribe") DeleteTribe diskDeleteTribe,
                             @Named("cloudSendTribe") SendTribe cloudSendTribe) {
        super(jobManager, diskSaveTribe, diskDeleteTribe, cloudSendTribe);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        diskDeleteTribeUsecase.unsubscribe();
        diskSaveTribeUsecase.unsubscribe();
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

    private void showFriendCollectionInView(List<Friendship> friendList) {
        this.homeGridView.renderFriendshipList(friendList);
    }

    @Override
    protected SendTribeView getView() {
        return homeGridView;
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
