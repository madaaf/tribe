package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.data.exception.JoinRoomException;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.game.GetDataChallengesGame;
import com.tribe.app.domain.interactor.game.GetNamesDrawGame;
import com.tribe.app.domain.interactor.game.GetNamesPostItGame;
import com.tribe.app.domain.interactor.live.BookRoomLink;
import com.tribe.app.domain.interactor.live.BuzzRoom;
import com.tribe.app.domain.interactor.live.DeclineInvite;
import com.tribe.app.domain.interactor.live.GetRoom;
import com.tribe.app.domain.interactor.live.GetRoomLink;
import com.tribe.app.domain.interactor.live.InviteUserToRoom;
import com.tribe.app.domain.interactor.live.RandomRoomAssigned;
import com.tribe.app.domain.interactor.live.RoomAcceptRandom;
import com.tribe.app.domain.interactor.user.CreateFriendship;
import com.tribe.app.domain.interactor.user.FbIdUpdated;
import com.tribe.app.domain.interactor.user.GetCloudUserInfosList;
import com.tribe.app.domain.interactor.user.GetDiskFriendshipList;
import com.tribe.app.domain.interactor.user.GetRecipientInfos;
import com.tribe.app.domain.interactor.user.IncrUserTimeInCall;
import com.tribe.app.domain.interactor.user.ReportUser;
import com.tribe.app.domain.interactor.user.UpdateFriendship;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import java.util.List;
import javax.inject.Inject;

public class LivePresenter extends FriendshipPresenter implements Presenter {

  // VIEW ATTACHED
  private LiveMVPView liveMVPView;

  // USECASES
  private GetDiskFriendshipList diskFriendshipList;
  private GetRoom getRoom;
  private BuzzRoom buzzRoom;
  private InviteUserToRoom inviteUserToRoom;
  private GetRecipientInfos getRecipientInfos;
  private GetCloudUserInfosList cloudUserInfosList;
  private GetRoomLink getRoomLink;
  private DeclineInvite declineInvite;
  private CreateFriendship createFriendship;
  private GetNamesPostItGame getNamesPostItGame;
  private GetNamesDrawGame getNamesDrawGame;
  private GetDataChallengesGame getDataChallengesGame;
  private BookRoomLink bookRoomLink;
  private RoomAcceptRandom roomAcceptRandom;
  private RandomRoomAssigned randomRoomAssigned;
  private FbIdUpdated fbIdUpdated;
  private ReportUser reportUser;
  private IncrUserTimeInCall incrUserTimeInCall;

  // SUBSCRIBERS
  private FriendshipListSubscriber diskFriendListSubscriber;
  private GetUserInfoListSubscriber getUserInfoListSubscriber;
  private CreateFriendshipSubscriber createFriendshipSubscriber;
  private RandomRoomAssignedSubscriber randomRoomAssignedSubscriber;
  private FbIdUpdatedSubscriber fbIdUpdatedSubscriber;

  @Inject
  public LivePresenter(GetDiskFriendshipList diskFriendshipList, GetRoom getRoom, BuzzRoom buzzRoom,
      InviteUserToRoom inviteUserToRoom, GetRecipientInfos getRecipientInfos,
      GetCloudUserInfosList cloudUserInfosList, GetRoomLink getRoomLink,
      DeclineInvite declineInvite, CreateFriendship createFriendship,
      GetNamesPostItGame getNamesPostItGame, UpdateFriendship updateFriendship,
      BookRoomLink bookRoomLink, RoomAcceptRandom roomAcceptRandom,
      RandomRoomAssigned randomRoomAssigned, ReportUser reportUser, FbIdUpdated fbIdUpdated,
      GetDataChallengesGame getDataChallengesGame, IncrUserTimeInCall incrUserTimeInCall,
      GetNamesDrawGame getNamesDrawGame) {
    this.updateFriendship = updateFriendship;
    this.diskFriendshipList = diskFriendshipList;
    this.getRoom = getRoom;
    this.buzzRoom = buzzRoom;
    this.inviteUserToRoom = inviteUserToRoom;
    this.getRecipientInfos = getRecipientInfos;
    this.cloudUserInfosList = cloudUserInfosList;
    this.getRoomLink = getRoomLink;
    this.declineInvite = declineInvite;
    this.createFriendship = createFriendship;
    this.getNamesPostItGame = getNamesPostItGame;
    this.bookRoomLink = bookRoomLink;
    this.roomAcceptRandom = roomAcceptRandom;
    this.randomRoomAssigned = randomRoomAssigned;
    this.reportUser = reportUser;
    this.incrUserTimeInCall = incrUserTimeInCall;
    this.fbIdUpdated = fbIdUpdated;
    this.getDataChallengesGame = getDataChallengesGame;
    this.getNamesDrawGame = getNamesDrawGame;
  }

  @Override public void onViewDetached() {
    super.onViewDetached();
    diskFriendshipList.unsubscribe();
    getRoom.unsubscribe();
    buzzRoom.unsubscribe();
    cloudUserInfosList.unsubscribe();
    inviteUserToRoom.unsubscribe();
    declineInvite.unsubscribe();
    getRecipientInfos.unsubscribe();
    getRoomLink.unsubscribe();
    createFriendship.unsubscribe();
    getNamesPostItGame.unsubscribe();
    bookRoomLink.unsubscribe();
    roomAcceptRandom.unsubscribe();
    randomRoomAssigned.unsubscribe();
    reportUser.unsubscribe();
    incrUserTimeInCall.unsubscribe();
    fbIdUpdated.unsubscribe();
    getDataChallengesGame.unsubscribe();
    getNamesDrawGame.unsubscribe();
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

  // TODO rethink
  //public void loadRecipient(Live live) {
  //  getRecipientInfos.prepare(live.getId());
  //  getRecipientInfos.execute(new RecipientInfosSubscriber());
  //}

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

  public void getRoomInfos(Live live) {
    getRoom.setup(live.getLinkId());
    getRoom.execute(new GetRoomSubscriber());
  }

  private final class GetRoomSubscriber extends DefaultSubscriber<Room> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      JoinRoomException joinRoomException = new JoinRoomException(e);
      String errorMessage = ErrorMessageFactory.create(liveMVPView.context(), joinRoomException);
      if (liveMVPView != null) liveMVPView.onRoomInfosError(errorMessage);
    }

    @Override public void onNext(Room room) {
      if (liveMVPView != null) {
        //if (roomConfiguration.getException() != null) {
        //  String errorMessage =
        //      ErrorMessageFactory.create(liveMVPView.context(), roomConfiguration.getException());
        //  if (roomConfiguration.getException() instanceof RoomFullException) {
        //    liveMVPView.onRoomFull(errorMessage);
        //  } else {
        //    liveMVPView.onJoinRoomError(errorMessage);
        //  }
        //} else {
        liveMVPView.onRoomInfos(room);
        //}
      }
    }
  }

  public void buzzRoom(String roomId) {
    buzzRoom.setup(roomId);
    buzzRoom.execute(new DefaultSubscriber());
  }

  public void inviteUserToRoom(String roomId, String userId) {
    if (userId.equals(Recipient.ID_CALL_ROULETTE)) return;
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

  public void getRoomLink(String roomId) {
    getRoomLink.setup(roomId);
    getRoomLink.execute(new GetRoomLinkSubscriber());
  }

  public void declineInvite(String roomId) {
    declineInvite.prepare(roomId);
    declineInvite.execute(new DefaultSubscriber());
  }

  public void incrementTimeInCall(String userId, Long timeInCall) {

    if (timeInCall != null) {
      incrUserTimeInCall.prepare(userId, timeInCall);
      incrUserTimeInCall.execute(new DefaultSubscriber());
    }
  }

  private final class GetRoomLinkSubscriber extends DefaultSubscriber<String> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(String roomLink) {
      liveMVPView.onRoomLink(roomLink);
    }
  }

  public void createFriendship(String userId) {
    if (createFriendshipSubscriber != null) createFriendshipSubscriber.unsubscribe();

    createFriendshipSubscriber = new CreateFriendshipSubscriber();
    createFriendship.setUserId(userId);
    createFriendship.execute(createFriendshipSubscriber);
  }

  private final class CreateFriendshipSubscriber extends DefaultSubscriber<Friendship> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Friendship friendship) {
      if (friendship == null) {
        liveMVPView.onAddError();
      } else {
        liveMVPView.onAddSuccess(friendship);
      }
    }
  }

  public void getNamesPostItGame(String lang) {
    getNamesPostItGame.setup(lang);
    getNamesPostItGame.execute(new GetNamesPostItGameSubscriber());
  }

  public void getNamesDrawGame(String lang) {
    getNamesDrawGame.setup(lang);
    getNamesDrawGame.execute(new GetNamesDrawGameSubscriber());
  }

  public void getDataChallengesGame(String lang) {
    getDataChallengesGame.setup(lang);
    getDataChallengesGame.execute(new GetDataChallengesGameSubscriber());
  }

  private final class RandomRoomAssignedSubscriber extends DefaultSubscriber<String> {
    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(String roomId) {
      //homeGridView.renderContactsOnApp(contactList);
      liveMVPView.randomRoomAssignedSubscriber(roomId);
    }
  }

  private final class GetNamesPostItGameSubscriber extends DefaultSubscriber<List<String>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<String> nameList) {
      liveMVPView.onNamesPostItGame(nameList);
    }
  }

  private final class GetNamesDrawGameSubscriber extends DefaultSubscriber<List<String>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<String> nameList) {
      liveMVPView.onNamesDrawGame(nameList);
    }
  }

  private final class GetDataChallengesGameSubscriber extends DefaultSubscriber<List<String>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<String> nameList) {
      liveMVPView.onDataChallengesGame(nameList);
    }
  }

  public void bookRoomLink(String linkId) {
    bookRoomLink.setLinkId(linkId);
    bookRoomLink.execute(new DefaultSubscriber());
  }

  public void roomAcceptRandom(String roomId) {
    roomAcceptRandom.setRoomId(roomId);
    roomAcceptRandom.execute(new DefaultSubscriber());
  }

  public void reportUser(String userId) {
    reportUser.setUserId(userId);
    reportUser.execute(new DefaultSubscriber());
  }

  public void randomRoomAssigned() {
    if (randomRoomAssignedSubscriber != null) {
      randomRoomAssignedSubscriber.unsubscribe();
    }

    randomRoomAssignedSubscriber = new RandomRoomAssignedSubscriber();
    randomRoomAssigned.execute(randomRoomAssignedSubscriber);
  }

  private final class FbIdUpdatedSubscriber extends DefaultSubscriber<User> {
    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(User userUpdated) {
      liveMVPView.fbIdUpdatedSubscriber(userUpdated);
    }
  }

  public void fbidUpdated() {
    if (fbIdUpdatedSubscriber != null) {
      fbIdUpdatedSubscriber.unsubscribe();
    }
    fbIdUpdatedSubscriber = new FbIdUpdatedSubscriber();
    fbIdUpdated.execute(fbIdUpdatedSubscriber);
  }
}
