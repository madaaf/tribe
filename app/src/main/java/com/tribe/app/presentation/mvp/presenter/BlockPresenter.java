package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.MoreType;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.DiskUpdateFriendship;
import com.tribe.app.domain.interactor.user.GetBlockedFriendshipList;
import com.tribe.app.presentation.mvp.view.BlockView;
import com.tribe.app.presentation.mvp.view.View;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by horatiothomas on 8/31/16.
 */
public class BlockPresenter implements Presenter {

    private BlockView blockView;

    private final DiskUpdateFriendship diskUpdateFriendship;
    private final GetBlockedFriendshipList getBlockedFriendshipList;

    private GetBlockedFriendshipListSubscriber getBlockedFriendshipListSubscriber;

    @Inject
    BlockPresenter(DiskUpdateFriendship diskUpdateFriendship,
                   GetBlockedFriendshipList getBlockedFriendshipList) {
        this.diskUpdateFriendship = diskUpdateFriendship;
        this.getBlockedFriendshipList = getBlockedFriendshipList;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {
        diskUpdateFriendship.unsubscribe();
        getBlockedFriendshipList.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        blockView = (BlockView) v;
    }

    @Override
    public void onCreate() {
        loadBlockedFriendshipList();
    }

    public void loadBlockedFriendshipList() {
        if (getBlockedFriendshipListSubscriber != null) getBlockedFriendshipListSubscriber.unsubscribe();

        getBlockedFriendshipListSubscriber = new GetBlockedFriendshipListSubscriber();
        getBlockedFriendshipList.execute(getBlockedFriendshipListSubscriber);
    }

    public void updateFriendship(String friendshipId) {
        diskUpdateFriendship.prepare(friendshipId, new MoreType("", MoreType.UNHIDE));
        diskUpdateFriendship.execute(new UpdateFriendshipSubscriber());
    }

    private class GetBlockedFriendshipListSubscriber extends DefaultSubscriber<List<Friendship>> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(List<Friendship> friendshipList) {
            blockView.renderBlockedFriendshipList(friendshipList);
        }
    }

    private class UpdateFriendshipSubscriber extends DefaultSubscriber<Friendship> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(Friendship friendship) {
            blockView.friendshipUpdated(friendship);
        }
    }
}
