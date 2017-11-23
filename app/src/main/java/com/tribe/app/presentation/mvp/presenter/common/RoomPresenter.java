package com.tribe.app.presentation.mvp.presenter.common;

import android.util.Pair;
import com.tribe.app.data.exception.JoinRoomException;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.live.BuzzRoom;
import com.tribe.app.domain.interactor.live.CreateInvite;
import com.tribe.app.domain.interactor.live.CreateRoom;
import com.tribe.app.domain.interactor.live.DeclineInvite;
import com.tribe.app.domain.interactor.live.DeleteRoom;
import com.tribe.app.domain.interactor.live.GetRoom;
import com.tribe.app.domain.interactor.live.RandomRoomAssigned;
import com.tribe.app.domain.interactor.live.RemoveInvite;
import com.tribe.app.domain.interactor.live.RoomUpdated;
import com.tribe.app.domain.interactor.live.UpdateRoom;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.presenter.Presenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.RoomMVPView;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class RoomPresenter implements Presenter {

  // VIEW ATTACHED
  private RoomMVPView roomMVPView;

  // USECASES
  private GetRoom getRoom;
  private CreateRoom createRoom;
  private UpdateRoom updateRoom;
  private DeleteRoom deleteRoom;
  private BuzzRoom buzzRoom;
  private CreateInvite createInvite;
  private RemoveInvite removeInvite;
  private DeclineInvite declineInvite;
  private RoomUpdated roomUpdated;
  private RandomRoomAssigned randomRoomAssigned;

  // SUBSCRIBERS
  private RandomRoomAssignedSubscriber randomRoomAssignedSubscriber;
  private RoomUpdatedSubscriber roomUpdatedSubscriber;

  @Inject public RoomPresenter(GetRoom getRoom, CreateRoom createRoom, UpdateRoom updateRoom,
      DeleteRoom deleteRoom, BuzzRoom buzzRoom, CreateInvite createInvite,
      RemoveInvite removeInvite, DeclineInvite declineInvite, RandomRoomAssigned randomRoomAssigned,
      RoomUpdated roomUpdated) {
    this.getRoom = getRoom;
    this.createRoom = createRoom;
    this.updateRoom = updateRoom;
    this.buzzRoom = buzzRoom;
    this.createInvite = createInvite;
    this.removeInvite = removeInvite;
    this.declineInvite = declineInvite;
    this.randomRoomAssigned = randomRoomAssigned;
    this.roomUpdated = roomUpdated;
    this.deleteRoom = deleteRoom;
  }

  @Override public void onViewDetached() {
    getRoom.unsubscribe();
    createRoom.unsubscribe();
    updateRoom.unsubscribe();
    deleteRoom.unsubscribe();
    buzzRoom.unsubscribe();
    createInvite.unsubscribe();
    removeInvite.unsubscribe();
    declineInvite.unsubscribe();
    randomRoomAssigned.unsubscribe();
    roomUpdated.unsubscribe();
    roomMVPView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    roomMVPView = (RoomMVPView) v;
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
      String errorMessage = ErrorMessageFactory.create(roomMVPView.context(), joinRoomException);
      if (roomMVPView != null) roomMVPView.onRoomInfosError(errorMessage);
    }

    @Override public void onNext(Room room) {
      if (roomMVPView != null) {
        roomMVPView.onRoomInfos(room);
      }
    }
  }

  public void createRoom(Live live) {
    createRoom.setup(null);
    createRoom.execute(new CreateRoomSubscriber());
  }

  private final class CreateRoomSubscriber extends DefaultSubscriber<Room> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      JoinRoomException joinRoomException = new JoinRoomException(e);
      String errorMessage = ErrorMessageFactory.create(roomMVPView.context(), joinRoomException);
      if (roomMVPView != null) roomMVPView.onRoomInfosError(errorMessage);
    }

    @Override public void onNext(Room room) {
      if (roomMVPView != null) {
        roomMVPView.onRoomInfos(room);
      }
    }
  }

  public void deleteRoom(String roomId) {
    deleteRoom.setup(roomId);
    deleteRoom.execute(new DefaultSubscriber());
  }

  public void buzzRoom(String roomId) {
    buzzRoom.setup(roomId);
    buzzRoom.execute(new DefaultSubscriber());
  }

  public void createInvite(String roomId, String... userIds) {
    createInvite.setup(roomId, userIds);
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

  public void roomAcceptRandom(String roomId) {
    Pair<String, String> pair = Pair.create(Room.ACCEPT_RANDOM, "true");
    List<Pair<String, String>> pairList = new ArrayList<>();
    pairList.add(pair);
    updateRoom.setup(roomId, pairList);
    updateRoom.execute(new DefaultSubscriber());
  }

  public void randomRoomAssigned() {
    if (randomRoomAssignedSubscriber != null) {
      randomRoomAssignedSubscriber.unsubscribe();
    }

    randomRoomAssignedSubscriber = new RandomRoomAssignedSubscriber();
    randomRoomAssigned.execute(randomRoomAssignedSubscriber);
  }

  private final class RandomRoomAssignedSubscriber extends DefaultSubscriber<String> {
    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(String roomId) {
      roomMVPView.randomRoomAssignedSubscriber(roomId);
    }
  }

  public void subscribeToRoomUpdates(String roomId) {
    if (roomUpdatedSubscriber != null) {
      roomUpdatedSubscriber.unsubscribe();
    }

    roomUpdatedSubscriber = new RoomUpdatedSubscriber();
    roomUpdated.setup(roomId);
    roomUpdated.execute(roomUpdatedSubscriber);
  }

  private final class RoomUpdatedSubscriber extends DefaultSubscriber<Room> {
    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Room room) {
      roomMVPView.onRoomUpdate(room);
    }
  }
}
