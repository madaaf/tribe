package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.data.exception.JoinRoomException;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.BuzzRoom;
import com.tribe.app.domain.interactor.user.GetCloudUserInfosList;
import com.tribe.app.domain.interactor.user.GetDiskFriendshipList;
import com.tribe.app.domain.interactor.user.GetRecipientInfos;
import com.tribe.app.domain.interactor.user.InviteUserToRoom;
import com.tribe.app.domain.interactor.user.JoinRoom;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class LivePresenter implements Presenter {

  // VIEW ATTACHED
  private LiveMVPView liveMVPView;

  // USECASES
  private GetDiskFriendshipList diskFriendshipList;
  private JoinRoom joinRoom;
  private BuzzRoom buzzRoom;
  private InviteUserToRoom inviteUserToRoom;
  private GetRecipientInfos getRecipientInfos;
  private GetCloudUserInfosList cloudUserInfosList;

  // SUBSCRIBERS
  private FriendshipListSubscriber diskFriendListSubscriber;
  private GetUserInfoListSubscriber getUserInfoListSubscriber;

  @Inject public LivePresenter(GetDiskFriendshipList diskFriendshipList, JoinRoom joinRoom,
      BuzzRoom buzzRoom, InviteUserToRoom inviteUserToRoom, GetRecipientInfos getRecipientInfos,
      GetCloudUserInfosList cloudUserInfosList) {
    this.diskFriendshipList = diskFriendshipList;
    this.joinRoom = joinRoom;
    this.buzzRoom = buzzRoom;
    this.inviteUserToRoom = inviteUserToRoom;
    this.getRecipientInfos = getRecipientInfos;
    this.cloudUserInfosList = cloudUserInfosList;
  }

  @Override public void onViewDetached() {
    diskFriendshipList.unsubscribe();
    joinRoom.unsubscribe();
    buzzRoom.unsubscribe();
    cloudUserInfosList.unsubscribe();
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

  public void loadRecipient(Live live) {
    getRecipientInfos.prepare(live.getId(), live.isGroup());
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

  public void joinRoom(Live live) {
    Timber.d("joinRoom");
    joinRoom.setup(!live.isGroup() ? live.getId() : live.getSubId(), live.isGroup(),
        live.getSessionId(), live.getLinkId());
    joinRoom.execute(new JoinRoomSubscriber());
  }

  private final class JoinRoomSubscriber extends DefaultSubscriber<RoomConfiguration> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      JoinRoomException joinRoomException = new JoinRoomException(e);
      String errorMessage = ErrorMessageFactory.create(liveMVPView.context(), joinRoomException);
      if (liveMVPView != null) liveMVPView.onJoinRoomFailed(errorMessage);
    }

    @Override public void onNext(RoomConfiguration roomConfiguration) {
      if (liveMVPView != null) {
        if (roomConfiguration.getException() != null) {
          String errorMessage =
              ErrorMessageFactory.create(liveMVPView.context(), roomConfiguration.getException());
          if (liveMVPView != null) liveMVPView.onJoinRoomFailed(errorMessage);
        } else {
          liveMVPView.onJoinedRoom(roomConfiguration);
        }
      }
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

  private final class GetUserInfoListSubscriber extends DefaultSubscriber<List<User>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(List<User> users) {
      super.onNext(users);
      liveMVPView.onReceivedAnonymousMemberInRoom(users);
    }
  }

  public void getUsersInfoListById(List<String> useridsList) {
    if (getUserInfoListSubscriber != null) getUserInfoListSubscriber.unsubscribe();
    getUserInfoListSubscriber = new GetUserInfoListSubscriber();

    cloudUserInfosList.setUserIdsList(useridsList);
    cloudUserInfosList.execute(getUserInfoListSubscriber);
  }
}
