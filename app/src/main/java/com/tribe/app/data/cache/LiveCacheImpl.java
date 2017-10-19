package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 01/27/2017.
 */
public class LiveCacheImpl implements LiveCache {

  private Context context;

  private ObservableRxHashMap<String, Boolean> onlineMap;
  private ObservableRxHashMap<String, Boolean> liveMap;
  private ObservableRxHashMap<String, Invite> inviteMap;
  private ObservableRxHashMap<String, Room> roomMap;
  private PublishSubject<String> roomCallRouletteMap = PublishSubject.create();
  private PublishSubject<User> onFbIdUpdated = PublishSubject.create();
  private PublishSubject<Room> onRoomUpdated = PublishSubject.create();

  @Inject public LiveCacheImpl(Context context) {
    this.context = context;
    onlineMap = new ObservableRxHashMap<>();
    liveMap = new ObservableRxHashMap<>();
    inviteMap = new ObservableRxHashMap<>();
    roomMap = new ObservableRxHashMap<>();
  }

  @Override public Observable<Map<String, Boolean>> onlineMap() {
    return onlineMap.getMapObservable().startWith(onlineMap.getMap());
  }

  @Override public Map<String, Boolean> getOnlineMap() {
    return onlineMap.getMap();
  }

  @Override public Observable<Map<String, Boolean>> liveMap() {
    return liveMap.getMapObservable().startWith(liveMap.getMap());
  }

  @Override public Map<String, Boolean> getLiveMap() {
    return liveMap.getMap();
  }

  @Override public void putOnline(String id) {
    onlineMap.put(id, true);
  }

  @Override public void removeOnline(String id) {
    onlineMap.remove(id, true);
  }

  @Override public void putLive(String id) {
    liveMap.put(id, true);
  }

  @Override public void removeLive(String id) {
    liveMap.remove(id, true);
  }

  @Override public void clearLive() {
    liveMap.clear();
  }

  @Override public void putInvite(Invite invite) {
    this.inviteMap.remove(invite.getId(), false);
    this.inviteMap.put(invite.getId(), invite);
  }

  @Override public void removeInvite(Invite invite) {
    this.inviteMap.remove(invite.getId(), true);
  }

  @Override public void removeInviteFromRoomId(String roomId) {
    this.inviteMap.remove(roomId, true);
  }

  @Override public Observable<Map<String, Invite>> inviteMap() {
    return inviteMap.getMapObservable().startWith(inviteMap.getMap());
  }

  @Override public Map<String, Invite> getInviteMap() {
    return inviteMap.getMap();
  }

  @Override public Observable<String> getRandomRoomAssignedValue() {
    return roomCallRouletteMap;
  }

  @Override public void putRandomRoomAssigned(String assignedRoomId) {
    roomCallRouletteMap.onNext(assignedRoomId);
  }

  @Override public void onFbIdUpdated(User userUpdated) {
    onFbIdUpdated.onNext(userUpdated);
  }

  @Override public Observable<User> getFbIdUpdated() {
    return onFbIdUpdated;
  }

  @Override public void putRoom(Room room) {
    roomMap.put(room.getId(), room);
  }

  @Override public void removeRoom(String roomId) {
    roomMap.remove(roomId, false);
  }

  @Override public void onRoomUpdated(Room roomUpdated) {
    Room room = null;
    // If it's an invite
    for (Invite invite : inviteMap.getMap().values()) {
      if (invite.getRoom() != null && invite.getRoom().getId().equals(roomUpdated)) {
        invite.getRoom().update(roomUpdated, true);
        room = invite.getRoom();
      }
    }

    // If room is null, it means it's an actual live room the user is in
    if (room == null) {
      if (roomMap.getMap().containsKey(roomUpdated.getId())) {
        room = roomMap.get(roomUpdated.getId());
        room.update(roomUpdated, true);
      } else {
        room = roomUpdated;
        roomMap.put(roomUpdated.getId(), roomUpdated);
      }
    }

    if (room != null) onRoomUpdated.onNext(room);
  }

  @Override public Observable<Room> getRoomUpdated() {
    return onRoomUpdated;
  }
}
