package com.tribe.app.data.repository.live.datasource;

import com.tribe.app.domain.entity.Room;
import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface LiveDataStore {

  Observable<Room> getRoom(String roomId);

  Observable<Room> createRoom(String name, String[] userIds);

  Observable<Void> deleteRoom(String roomId);

  Observable<Boolean> inviteUserToRoom(String roomId, String userId);

  Observable<Boolean> buzzRoom(String roomId);

  Observable<Void> declineInvite(String roomId);

  Observable<String> getRoomLink(String roomId);

  Observable<Boolean> bookRoomLink(String linkId);

  Observable<Void> roomAcceptRandom(String roomId);

  Observable<String> randomRoomAssigned();
}
