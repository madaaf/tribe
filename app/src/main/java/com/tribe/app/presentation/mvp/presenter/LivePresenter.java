package com.tribe.app.presentation.mvp.presenter;

import android.util.Pair;
import com.tribe.app.data.exception.JoinRoomException;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.game.GetDataChallengesGame;
import com.tribe.app.domain.interactor.game.GetNamesDrawGame;
import com.tribe.app.domain.interactor.game.GetNamesPostItGame;
import com.tribe.app.domain.interactor.live.BuzzRoom;
import com.tribe.app.domain.interactor.live.CreateInvite;
import com.tribe.app.domain.interactor.live.CreateRoom;
import com.tribe.app.domain.interactor.live.DeclineInvite;
import com.tribe.app.domain.interactor.live.GetRoom;
import com.tribe.app.domain.interactor.live.RandomRoomAssigned;
import com.tribe.app.domain.interactor.live.RemoveInvite;
import com.tribe.app.domain.interactor.live.RoomUpdated;
import com.tribe.app.domain.interactor.live.UpdateRoom;
import com.tribe.app.domain.interactor.user.FbIdUpdated;
import com.tribe.app.domain.interactor.user.GetCloudUserInfosList;
import com.tribe.app.domain.interactor.user.GetRecipientInfos;
import com.tribe.app.domain.interactor.user.IncrUserTimeInCall;
import com.tribe.app.domain.interactor.user.ReportUser;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class LivePresenter implements Presenter {

  // COMPOSITE PRESENTERS
  private ShortcutPresenter shortcutPresenter;

  // VIEW ATTACHED
  private LiveMVPView liveMVPView;

  // USECASES
  private GetRoom getRoom;
  private CreateRoom createRoom;
  private UpdateRoom updateRoom;
  private BuzzRoom buzzRoom;
  private CreateInvite createInvite;
  private RemoveInvite removeInvite;
  private DeclineInvite declineInvite;
  private GetRecipientInfos getRecipientInfos;
  private GetCloudUserInfosList cloudUserInfosList;
  private GetNamesPostItGame getNamesPostItGame;
  private GetNamesDrawGame getNamesDrawGame;
  private GetDataChallengesGame getDataChallengesGame;
  private RandomRoomAssigned randomRoomAssigned;
  private FbIdUpdated fbIdUpdated;
  private ReportUser reportUser;
  private IncrUserTimeInCall incrUserTimeInCall;
  private RoomUpdated roomUpdated;

  // SUBSCRIBERS
  private GetUserInfoListSubscriber getUserInfoListSubscriber;
  private RandomRoomAssignedSubscriber randomRoomAssignedSubscriber;
  private FbIdUpdatedSubscriber fbIdUpdatedSubscriber;
  private RoomUpdatedSubscriber roomUpdatedSubscriber;

  @Inject
  public LivePresenter(ShortcutPresenter shortcutPresenter, GetRoom getRoom, CreateRoom createRoom,
      UpdateRoom updateRoom, BuzzRoom buzzRoom, CreateInvite createInvite,
      RemoveInvite removeInvite, DeclineInvite declineInvite, GetRecipientInfos getRecipientInfos,
      GetCloudUserInfosList cloudUserInfosList, GetNamesPostItGame getNamesPostItGame,
      RandomRoomAssigned randomRoomAssigned, ReportUser reportUser, FbIdUpdated fbIdUpdated,
      GetDataChallengesGame getDataChallengesGame, IncrUserTimeInCall incrUserTimeInCall,
      GetNamesDrawGame getNamesDrawGame, RoomUpdated roomUpdated) {
    this.shortcutPresenter = shortcutPresenter;
    this.getRoom = getRoom;
    this.createRoom = createRoom;
    this.updateRoom = updateRoom;
    this.buzzRoom = buzzRoom;
    this.createInvite = createInvite;
    this.removeInvite = removeInvite;
    this.declineInvite = declineInvite;
    this.getRecipientInfos = getRecipientInfos;
    this.cloudUserInfosList = cloudUserInfosList;
    this.getNamesPostItGame = getNamesPostItGame;
    this.randomRoomAssigned = randomRoomAssigned;
    this.reportUser = reportUser;
    this.incrUserTimeInCall = incrUserTimeInCall;
    this.fbIdUpdated = fbIdUpdated;
    this.getDataChallengesGame = getDataChallengesGame;
    this.getNamesDrawGame = getNamesDrawGame;
    this.roomUpdated = roomUpdated;
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    getRoom.unsubscribe();
    createRoom.unsubscribe();
    updateRoom.unsubscribe();
    buzzRoom.unsubscribe();
    cloudUserInfosList.unsubscribe();
    createInvite.unsubscribe();
    removeInvite.unsubscribe();
    declineInvite.unsubscribe();
    getRecipientInfos.unsubscribe();
    getNamesPostItGame.unsubscribe();
    randomRoomAssigned.unsubscribe();
    reportUser.unsubscribe();
    incrUserTimeInCall.unsubscribe();
    fbIdUpdated.unsubscribe();
    getDataChallengesGame.unsubscribe();
    getNamesDrawGame.unsubscribe();
    roomUpdated.unsubscribe();
    liveMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    liveMVPView = (LiveMVPView) v;
    shortcutPresenter.onViewAttached(v);
  }

  public void getRoomInfos(Live live) {
    getRoom.setup(live);
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

  public void createRoom(Live live) {
    createRoom.setup(null, live.getUserIds().toArray(new String[live.getUserIds().size()]));
    createRoom.execute(new CreateRoomSubscriber());
  }

  private final class CreateRoomSubscriber extends DefaultSubscriber<Room> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(Room room) {
      if (liveMVPView != null) {
        liveMVPView.onRoomInfos(room);
      }
    }
  }

  public void buzzRoom(String roomId) {
    buzzRoom.setup(roomId);
    buzzRoom.execute(new DefaultSubscriber());
  }

  public void createInvite(String roomId, String userId) {
    if (userId.equals(Recipient.ID_CALL_ROULETTE)) return;
    createInvite.setup(roomId, userId);
    createInvite.execute(new DefaultSubscriber());
  }

  public void removeInvite(String roomId, String userId) {
    removeInvite.setup(roomId, userId);
    removeInvite.execute(new DefaultSubscriber());
  }

  public void declineInvite(String roomId) {
    declineInvite.setup(roomId);
    declineInvite.execute(new DefaultSubscriber());
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

  public void incrementTimeInCall(String userId, Long timeInCall) {

    if (timeInCall != null) {
      incrUserTimeInCall.prepare(userId, timeInCall);
      incrUserTimeInCall.execute(new DefaultSubscriber());
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

  public void roomAcceptRandom(String roomId) {
    Pair<String, String> pair = Pair.create(Room.ACCEPT_RANDOM, "true");
    List<Pair<String, String>> pairList = new ArrayList<>();
    pairList.add(pair);
    updateRoom.setup(roomId, pairList);
    updateRoom.execute(new DefaultSubscriber());
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

  public void subscribeToRoomUpdates() {
    if (roomUpdatedSubscriber != null) {
      roomUpdatedSubscriber.unsubscribe();
    }

    roomUpdatedSubscriber = new RoomUpdatedSubscriber();
    roomUpdated.execute(roomUpdatedSubscriber);
  }

  private final class RoomUpdatedSubscriber extends DefaultSubscriber<Room> {
    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Room room) {
      liveMVPView.onRoomUpdate(room);
    }
  }

  public void createShortcut(String... userIds) {
    shortcutPresenter.createShortcut(userIds);
  }

  public void muteShortcut(String shortcutId, boolean mute) {
    shortcutPresenter.muteShortcut(shortcutId, mute);
  }

  public void updateShortcutStatus(String shortcutId, @ShortcutRealm.ShortcutStatus String status) {
    shortcutPresenter.updateShortcutStatus(shortcutId, status);
  }

  public void readShortcut(String shortcutId) {
    shortcutPresenter.readShortcut(shortcutId);
  }

  public void pinShortcut(String shortcutId, boolean pinned) {
    shortcutPresenter.pinShortcut(shortcutId, pinned);
  }

  public void removeShortcut(String shortcutId) {
    shortcutPresenter.removeShortcut(shortcutId);
  }

  public void loadSingleShortcuts() {
    shortcutPresenter.loadSingleShortcuts();
  }
}
