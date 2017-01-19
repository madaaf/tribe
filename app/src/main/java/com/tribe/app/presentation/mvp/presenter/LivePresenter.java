package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.GetDiskFriendshipList;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;

import java.util.List;

import javax.inject.Inject;

public class LivePresenter implements Presenter {

    // VIEW ATTACHED
    private LiveMVPView liveMVPView;

    // USECASES
    private GetDiskFriendshipList diskFriendshipList;

    // SUBSCRIBERS
    private FriendshipListSubscriber diskFriendListSubscriber;

    @Inject
    public LivePresenter(GetDiskFriendshipList diskFriendshipList) {
        this.diskFriendshipList = diskFriendshipList;
    }

    @Override
    public void onViewDetached() {
        diskFriendshipList.unsubscribe();
    }

    @Override
    public void onViewAttached(MVPView v) {
        liveMVPView = (LiveMVPView) v;
        loadFriendshipList();
    }

    public void loadFriendshipList() {
        if (diskFriendListSubscriber != null) {
            diskFriendListSubscriber.unsubscribe();
        }

        diskFriendListSubscriber = new FriendshipListSubscriber();
        diskFriendshipList.execute(diskFriendListSubscriber);
    }

    private final class FriendshipListSubscriber extends DefaultSubscriber<List<Friendship>> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(List<Friendship> friendshipList) {
            liveMVPView.renderFriendshipList(friendshipList);
        }
    }
}
