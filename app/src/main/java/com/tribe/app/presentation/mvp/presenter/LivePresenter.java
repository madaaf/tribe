package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.BuzzRoom;
import com.tribe.app.domain.interactor.user.GetDiskFriendshipList;
import com.tribe.app.domain.interactor.user.GetRecipientInfos;
import com.tribe.app.domain.interactor.user.InviteUserToRoom;
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
  private BuzzRoom buzzRoom;
  private InviteUserToRoom inviteUserToRoom;
  private GetRecipientInfos getRecipientInfos;

  // SUBSCRIBERS
  private FriendshipListSubscriber diskFriendListSubscriber;

  @Inject public LivePresenter(GetDiskFriendshipList diskFriendshipList, JoinRoom joinRoom,
      BuzzRoom buzzRoom, InviteUserToRoom inviteUserToRoom, GetRecipientInfos getRecipientInfos) {
    this.diskFriendshipList = diskFriendshipList;
    this.joinRoom = joinRoom;
    this.buzzRoom = buzzRoom;
    this.inviteUserToRoom = inviteUserToRoom;
    this.getRecipientInfos = getRecipientInfos;
  }

  @Override public void onViewDetached() {
    diskFriendshipList.unsubscribe();
    joinRoom.unsubscribe();
    buzzRoom.unsubscribe();
    inviteUserToRoom.unsubscribe();
    getRecipientInfos.unsubscribe();
    liveMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    liveMVPView = (LiveMVPView) v;
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

  public void loadRecipient(String recipientId, boolean isGroup) {
    getRecipientInfos.prepare(recipientId, isGroup);
    getRecipientInfos.execute(new RecipientInfosSubscriber());
  }

  private final class RecipientInfosSubscriber extends DefaultSubscriber<Recipient> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Recipient recipient) {
      liveMVPView.onRecipientInfos(recipient);
    }
  }

  public void joinRoom(Recipient recipient, String roomId) {
    boolean isGroup = recipient instanceof Membership;
    joinRoom.setup(!isGroup ? recipient.getId() : recipient.getSubId(), isGroup, roomId);
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

  public void buzzRoom(String roomId) {
    buzzRoom.setup(roomId);
    buzzRoom.execute(new DefaultSubscriber());
  }

  public void inviteUserToRoom(String roomId, String userId) {
    inviteUserToRoom.setup(roomId, userId);
    inviteUserToRoom.execute(new DefaultSubscriber());
  }
}
