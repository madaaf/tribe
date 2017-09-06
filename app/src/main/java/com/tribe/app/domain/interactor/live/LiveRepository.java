package com.tribe.app.domain.interactor.live;

/**
 * Created by tiago on 12/28/2016.
 */

import android.util.Pair;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import java.util.List;
import rx.Observable;

/**
 * Interface that represents a Repository for live
 */
public interface LiveRepository {

  Observable<Room> getRoom(Live live);

  Observable<Room> createRoom(String name, String... userIds);

  Observable<Room> updateRoom(String roomId, List<Pair<String, String>> pairList);

  Observable<Void> deleteRoom(String roomId);

  Observable<Boolean> createInvite(String roomId, String... userIds);

  Observable<Boolean> removeInvite(String roomId, String userId);

  Observable<Boolean> declineInvite(String roomId);

  Observable<Boolean> buzzRoom(String roomId);

  Observable<String> randomRoomAssigned();

  Observable<Room> getRoomUpdated();
}
