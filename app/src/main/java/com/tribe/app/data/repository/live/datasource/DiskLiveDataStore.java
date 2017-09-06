package com.tribe.app.data.repository.live.datasource;

import android.util.Pair;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import java.util.List;
import java.util.Map;
import rx.Observable;

public class DiskLiveDataStore
    implements LiveDataStore, com.tribe.app.data.repository.user.datasource.LiveDataStore {

  private final LiveCache liveCache;

  public DiskLiveDataStore(LiveCache liveCache) {
    this.liveCache = liveCache;
  }

  @Override public Observable<Room> getRoom(Live live) {
    return null;
  }

  @Override public Observable<Room> createRoom(String name, String[] userIds) {
    return null;
  }

  @Override public Observable<Room> updateRoom(String roomId, List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<Void> deleteRoom(String roomId) {
    return null;
  }

  @Override public Observable<Boolean> createInvite(String roomId, String[] userIds) {
    return null;
  }

  @Override public Observable<Boolean> removeInvite(String roomId, String userId) {
    return null;
  }

  @Override public Observable<Boolean> declineInvite(String roomId) {
    return null;
  }

  @Override public Observable<Boolean> buzzRoom(String roomId) {
    return null;
  }

  @Override public Observable<String> randomRoomAssigned() {
    return liveCache.getRandomRoomAssignedValue();
  }

  @Override public Observable<Map<String, Boolean>> onlineMap() {
    return null;
  }

  @Override public Observable<Map<String, Boolean>> liveMap() {
    return null;
  }

  @Override public Observable<Map<String, Invite>> inviteMap() {
    return null;
  }

  @Override public Observable<User> getFbIdUpdated() {
    return null;
  }

  @Override public Observable<Room> getRoomUpdated() {
    return liveCache.getRoomUpdated();
  }
}
