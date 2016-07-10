package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;
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
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

public class HomeGridPresenter extends SendTribePresenter implements Presenter {

    private UseCase cloudUserInfosUsecase;
    private UseCase diskUserInfosUsecase;
    private UseCase cloudGetTribeListUsecase;
    private UseCase diskGetTribeListUsecase;

    private HomeGridView homeGridView;

    @Inject
    public HomeGridPresenter(JobManager jobManager,
                             @Named("cloudUserInfos") UseCase cloudUserInfos,
                             @Named("diskUserInfos") UseCase diskUserInfos,
                             @Named("diskSaveTribe") SaveTribe diskSaveTribe,
                             @Named("diskDeleteTribe") DeleteTribe diskDeleteTribe,
                             @Named("cloudSendTribe") SendTribe cloudSendTribe,
                             @Named("cloudGetTribes") UseCase cloudGetTribeList,
                             @Named("diskGetTribes") UseCase diskGetTribeList) {
        super(jobManager, diskSaveTribe, diskDeleteTribe, cloudSendTribe);
        this.cloudUserInfosUsecase = cloudUserInfos;
        this.diskUserInfosUsecase = diskUserInfos;
        this.cloudGetTribeListUsecase = cloudGetTribeList;
        this.diskGetTribeListUsecase = diskGetTribeList;
    }

    @Override
    public void onCreate() {
        loadFriendList();
        loadTribeList();
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
        cloudGetTribeListUsecase.unsubscribe();
        diskGetTribeListUsecase.unsubscribe();
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

    public void loadTribeList() {
        TribeListSubscriber subscriber = new TribeListSubscriber();
        //diskUserInfosUsecase.execute(subscriber);
        cloudGetTribeListUsecase.execute(subscriber);
    }

    private void showFriendCollectionInView(List<Friendship> friendList) {
        this.homeGridView.renderFriendshipList(friendList);
    }

    private void updateTribes(Map<Friendship, List<Tribe>> tribes) {
        this.homeGridView.updateTribes(tribes);
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

    private final class TribeListSubscriber extends DefaultSubscriber<Map<Friendship, List<Tribe>>> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(Map<Friendship, List<Tribe>> tribes) {
            updateTribes(tribes);
        }
    }
}
