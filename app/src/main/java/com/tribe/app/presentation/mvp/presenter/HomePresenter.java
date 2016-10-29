package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.UpdateMessagesJob;
import com.tribe.app.data.network.job.UpdateScoreJob;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.CreateMembership;
import com.tribe.app.domain.interactor.user.GetHeadDeepLink;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import javax.inject.Inject;
import javax.inject.Named;

public class HomePresenter implements Presenter {

    private SendToken sendTokenUseCase;
    private UseCase cloudUserInfos;
    private JobManager jobManager;
    private GetHeadDeepLink getHeadDeepLink;
    private CreateMembership createMembership;

    private FriendListSubscriber friendListSubscriber;

    private HomeView homeView;

    @Inject
    public HomePresenter(JobManager jobManager,
                         @Named("sendToken") SendToken sendToken,
                         @Named("cloudUserInfos") UseCase cloudUserInfos,
                         GetHeadDeepLink getHeadDeepLink,
                         CreateMembership createMembership) {
        this.sendTokenUseCase = sendToken;
        this.cloudUserInfos = cloudUserInfos;
        this.jobManager = jobManager;
        this.getHeadDeepLink = getHeadDeepLink;
        this.createMembership = createMembership;
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
        getHeadDeepLink.unsubscribe();
        createMembership.unsubscribe();
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

    public void getHeadDeepLink(String url) {
        getHeadDeepLink.prepare(url);
        getHeadDeepLink.execute(new GetHeadDeepLinkSubscriber());
    }

    public void createMembership(String groupId) {
        createMembership.setGroupId(groupId);
        createMembership.execute(new CreateMembershipSubscriber());
    }

    public void updateScoreLocation() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.LOCATION, 1));
    }

    public void updateScoreCamera() {
        jobManager.addJobInBackground(new UpdateScoreJob(ScoreUtils.Point.CAMERA, 1));
    }

    @Override
    public void attachView(View v) {
        homeView = (HomeView) v;
    }

    private final class FriendListSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {
            homeView.hideLoading();
        }

        @Override
        public void onError(Throwable e) {
            homeView.hideLoading();
        }

        @Override
        public void onNext(User user) {
            homeView.hideLoading();
            jobManager.addJobInBackground(new UpdateMessagesJob());
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

    private final class GetHeadDeepLinkSubscriber extends DefaultSubscriber<String> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(String url) {
            if (!StringUtils.isEmpty(url)) homeView.onDeepLink(url);
        }
    }

    private final class CreateMembershipSubscriber extends DefaultSubscriber<Membership> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Membership membership) {
            homeView.onMembershipCreated(membership);
        }
    }
}
