package com.tribe.app.domain.interactor.live;

/**
 * Created by tiago on 12/28/2016.
 */

import android.util.Pair;
import com.tribe.app.domain.entity.Room;
import java.util.List;
import rx.Observable;

/**
 * Interface that represents a Repository for live
 */
public interface LiveRepository {

  Observable<Room> getRoom(String roomId);

  Observable<Room> createRoom(String name, String... userIds);

  Observable<Room> updateRoom(String roomId, List<Pair<String, String>> pairList);

  Observable<Void> deleteRoom(String roomId);

  Observable<Boolean> inviteUserToRoom(String roomId, String userId);

  Observable<Boolean> dismissInvite(String roomId, String userId);

  Observable<Boolean> buzzRoom(String roomId);

  Observable<Void> declineInvite(String roomId);

  Observable<Boolean> bookRoomLink(String linkId);

  Observable<String> randomRoomAssigned();

  Observable<Room> getRoomUpdated();
}
