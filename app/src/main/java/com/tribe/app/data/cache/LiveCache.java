package com.tribe.app.data.cache;

import com.tribe.app.data.realm.UserPlayingRealm;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import java.util.Map;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton public interface LiveCache {

  Observable<Map<String, Boolean>> onlineMap();

  Map<String, Boolean> getOnlineMap();

  Observable<Map<String, UserPlayingRealm>> playingMap();

  Map<String, UserPlayingRealm> getPlayingMap();

  Observable<Map<String, Boolean>> liveMap();

  Map<String, Boolean> getLiveMap();

  void putOnline(String id);

  void removeOnline(String id);

  void putPlaying(String id, UserPlayingRealm userPlayingRealm);

  void removePlaying(String id);

  void putLive(String id);

  void removeLive(String id);

  void clearLive();

  void putInvite(Invite invite);

  void removeInvite(Invite invite);

  void removeInviteFromRoomId(String roomId);

  Observable<Map<String, Invite>> inviteMap();

  Map<String, Invite> getInviteMap();

  Observable<String> getRandomRoomAssignedValue();

  void putRandomRoomAssigned(String assignedRoomId);

  void onFbIdUpdated(User userUpdated);

  Observable<User> getFbIdUpdated();

  void putRoom(Room room);

  void removeRoom(String roomId);

  void onRoomUpdated(Room roomUpdated);

  Observable<Room> getRoomUpdated();
}
