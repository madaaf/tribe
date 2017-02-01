package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.GetDiskFriendshipList;
import com.tribe.app.domain.interactor.user.JoinRoom;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import java.util.List;
import javax.inject.Inject;

public class LivePresenter implements Presenter {

  // VIEW ATTACHED
  private LiveMVPView liveMVPView;

  // USECASES
  private GetDiskFriendshipList diskFriendshipList;
  private JoinRoom joinRoom;

  // SUBSCRIBERS
  private FriendshipListSubscriber diskFriendListSubscriber;
  private JoinRoomSubscriber joinRoomSubscriber;

  @Inject public LivePresenter(GetDiskFriendshipList diskFriendshipList, JoinRoom joinRoom) {
    this.diskFriendshipList = diskFriendshipList;
    this.joinRoom = joinRoom;
  }

  @Override public void onViewDetached() {
    diskFriendshipList.unsubscribe();
    joinRoom.unsubscribe();
  }

  @Override public void onViewAttached(MVPView v) {
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

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<Friendship> friendshipList) {
      liveMVPView.renderFriendshipList(friendshipList);
    }
  }

  public void joinRoom(Recipient recipient) {
    boolean isGroup = recipient instanceof Membership;
    joinRoom.setup(!isGroup ? recipient.getId() : recipient.getSubId(), isGroup);
    joinRoom.execute(new JoinRoomSubscriber());
  }

  private final class JoinRoomSubscriber extends DefaultSubscriber<RoomConfiguration> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(RoomConfiguration roomConfiguration) {
      liveMVPView.onJoinedRoom(roomConfiguration);
    }
  }
}
