package com.tribe.app.data.repository.live.datasource;

import android.util.Pair;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import java.util.List;
import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface LiveDataStore {

  Observable<Room> getRoom(Live live);

  Observable<Room> createRoom(String name, String[] userIds);

  Observable<Room> updateRoom(String roomId, List<Pair<String, String>> values);

  Observable<Void> deleteRoom(String roomId);

  Observable<Boolean> createInvite(String roomId, String userId);

  Observable<Boolean> removeInvite(String roomId, String userId);

  Observable<Boolean> declineInvite(String roomId);

  Observable<Boolean> buzzRoom(String roomId);

  Observable<String> randomRoomAssigned();
}
