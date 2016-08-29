package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.UpdateTribeListNotSeenStatusJob;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class HomePresenter implements Presenter {

    private SendToken sendTokenUseCase;
    private UseCase cloudUserInfos;
    private JobManager jobManager;

    private FriendListSubscriber friendListSubscriber;

    private HomeView homeView;

    @Inject
    public HomePresenter(JobManager jobManager,
                         @Named("sendToken") SendToken sendToken,
                         @Named("cloudUserInfos") UseCase cloudUserInfos) {
        this.sendTokenUseCase = sendToken;
        this.cloudUserInfos = cloudUserInfos;
        this.jobManager = jobManager;
    }

    @Override
    public void onCreate() {

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
        sendTokenUseCase.unsubscribe();
        cloudUserInfos.unsubscribe();
    }

    public void sendToken(String token) {
        sendTokenUseCase.setToken(token);
        sendTokenUseCase.execute(new SendTokenSubscriber());
    }

    public void reloadData() {
        homeView.showLoading();
        friendListSubscriber = new FriendListSubscriber();
        cloudUserInfos.execute(friendListSubscriber);
    }

    public void updateMessagesToNotSeen(List<Message> messageList) {
        jobManager.addJobInBackground(new UpdateTribeListNotSeenStatusJob(messageList));
    }

    @Override
    public void attachView(View v) {
        homeView = (HomeView) v;
    }

    private final class FriendListSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(User user) {
            homeView.hideLoading();
        }
    }

    private final class SendTokenSubscriber extends DefaultSubscriber<Installation> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(Installation installation) {
            // TODO WHATEVER NEEDS TO BE DONE
        }
    }
}
