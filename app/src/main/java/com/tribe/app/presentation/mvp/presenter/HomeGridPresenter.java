package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.UpdateTribesJob;
import com.tribe.app.data.network.job.UpdateUserJob;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Tribe;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import com.tribe.app.domain.interactor.tribe.DeleteTribe;
import com.tribe.app.domain.interactor.tribe.SaveTribe;
import com.tribe.app.presentation.mvp.view.HomeGridView;
import com.tribe.app.presentation.mvp.view.SendTribeView;
import com.tribe.app.presentation.mvp.view.View;
import com.tribe.app.presentation.utils.MessageStatus;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class HomeGridPresenter extends SendTribePresenter implements Presenter {

    // VIEW ATTACHED
    private HomeGridView homeGridView;

    // USECASES
    private UseCaseDisk diskUserInfosUsecase;
    private UseCaseDisk diskGetTribeListUsecase;
    private UseCaseDisk diskGetPendingTribeListUsecase;

    // SUBSCRIBERS
    private TribePendingListSubscriber tribePendingListSubscriber;

    // VARIABLES
    private boolean forceUpdate = true;

    @Inject
    public HomeGridPresenter(JobManager jobManager,
                             @Named("diskUserInfos") UseCaseDisk diskUserInfos,
                             @Named("diskSaveTribe") SaveTribe diskSaveTribe,
                             @Named("diskDeleteTribe") DeleteTribe diskDeleteTribe,
                             @Named("diskGetTribes") UseCaseDisk diskGetTribeList,
                             @Named("diskGetPendingTribes") UseCaseDisk diskGetPendingTribeList) {
        super(jobManager, diskSaveTribe, diskDeleteTribe);
        this.diskUserInfosUsecase = diskUserInfos;
        this.diskGetTribeListUsecase = diskGetTribeList;
        this.diskGetPendingTribeListUsecase = diskGetPendingTribeList;
    }

    @Override
    public void onCreate() {
        loadFriendList();
        loadPendingTribeList();
        jobManager.addJobInBackground(new UpdateUserJob());
        jobManager.addJobInBackground(new UpdateTribesJob());
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
        diskGetTribeListUsecase.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        homeGridView = (HomeGridView) v;
    }

    public void loadFriendList() {
        showViewLoading();
        FriendListSubscriber subscriber = new FriendListSubscriber();
        diskUserInfosUsecase.execute(subscriber);
    }

    public void loadTribeList() {
        TribeListSubscriber subscriber = new TribeListSubscriber();
        diskGetTribeListUsecase.execute(subscriber);
    }

    public void loadPendingTribeList() {
        if (tribePendingListSubscriber == null) {
            tribePendingListSubscriber = new TribePendingListSubscriber();
        }

        diskGetPendingTribeListUsecase.execute(tribePendingListSubscriber);
    }

    private void showFriendCollectionInView(List<Friendship> friendList) {
        this.homeGridView.renderFriendshipList(friendList);
    }

    private void updateTribes(List<Tribe> tribes) {
        for (Tribe tribe : tribes) {
            if (tribe.getMessageStatus().equals(MessageStatus.STATUS_RECEIVED)) {
                //jobManager.addJobInBackground(new DownloadTribeJob(tribe));
            }
        }

        this.homeGridView.updateTribes(tribes);
    }

    private void futureUpdateTribes(List<Tribe> tribes) {
        this.homeGridView.futureUpdateTribes(tribes);
    }

    private void updatePendingTribes(List<Tribe> tribes) {
        this.homeGridView.updatePendingTribes(tribes);
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
            List<Friendship> friendships = user.getFriendshipList();
            friendships.add(0, user);
            showFriendCollectionInView(friendships);

            if (friendships.size() > 1) loadTribeList();
        }
    }

    private final class TribeListSubscriber extends DefaultSubscriber<List<Tribe>> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(List<Tribe> tribes) {
            updateTribes(tribes);
        }
    }

    private final class TribePendingListSubscriber extends DefaultSubscriber<List<Tribe>> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(List<Tribe> tribes) {
            updatePendingTribes(tribes);
        }
    }
}
