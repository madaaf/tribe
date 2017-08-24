package com.tribe.app.domain.interactor.live;

/**
 * Created by tiago on 12/28/2016.
 */

import com.tribe.app.domain.entity.Room;
import rx.Observable;

/**
 * Interface that represents a Repository for live
 */
public interface LiveRepository {

  Observable<Room> getRoom(String roomId);

  Observable<Room> createRoom(String name, String... userIds);

  Observable<Void> deleteRoom(String roomId);

  Observable<Boolean> inviteUserToRoom(String roomId, String userId);

  Observable<Boolean> buzzRoom(String roomId);

  Observable<Void> declineInvite(String roomId);

  Observable<String> getRoomLink(String roomId);

  Observable<Boolean> bookRoomLink(String linkId);

  Observable<Void> roomAcceptRandom(String roomId);

  Observable<String> randomRoomAssigned();
}
