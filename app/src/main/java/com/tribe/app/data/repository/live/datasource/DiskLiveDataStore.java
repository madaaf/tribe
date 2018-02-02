package com.tribe.app.data.repository.live.datasource;

import android.util.Pair;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.network.entity.RemoveMessageEntity;
import com.tribe.app.data.realm.UserPlayingRealm;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.entity.UserPlaying;
import java.util.List;
import java.util.Map;
import rx.Observable;

public class DiskLiveDataStore
    implements LiveDataStore, com.tribe.app.data.repository.user.datasource.LiveDataStore {

  private LiveCache liveCache;

  public DiskLiveDataStore(LiveCache liveCache) {
    this.liveCache = liveCache;
  }

  @Override public Observable<Room> getRoom(Live live) {
    return null;
  }

  @Override public Observable<Room> createRoom(String name, String gameId) {
    return null;
  }

  @Override public Observable<Room> updateRoom(String roomId, List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<Void> deleteRoom(String roomId) {
    return null;
  }

  @Override public Observable<Boolean> createInvite(String roomId, String... userId) {
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

  @Override public Observable<RemoveMessageEntity> removeMessage(String messageId) {
    return null;
  }

  @Override public Observable<String> randomRoomAssigned() {
    return liveCache.getRandomRoomAssignedValue();
  }

  @Override public Observable<Map<String, Boolean>> onlineMap() {
    return liveCache.onlineMap();
  }

  @Override public Observable<Map<String, UserPlayingRealm>> playingMap() {
    return liveCache.playingMap();
  }

  @Override public Observable<Map<String, Boolean>> liveMap() {
    return liveCache.liveMap();
  }

  @Override public Observable<Map<String, Invite>> inviteMap() {
    return liveCache.inviteMap();
  }

  @Override public Observable<User> getFbIdUpdated() {
    return null;
  }

  @Override public Observable<Room> getRoomUpdated(String roomId) {
    return Observable.combineLatest(liveCache.getRoomUpdated().startWith(Observable.empty()),
        liveCache.liveMap(), liveCache.onlineMap(), (room, liveMap, onlineMap) -> room)
        .filter(room -> room.getId().equals(roomId))
        .compose(onlineLiveTransformer);
  }

  private Observable.Transformer<Room, Room> onlineLiveTransformer =
      roomObservable -> roomObservable.map(room -> {
        Map<String, Boolean> onlineMap = liveCache.getOnlineMap();

        if (room.getLiveUsers() != null) {
          for (User user : room.getLiveUsers()) {
            user.setIsOnline(onlineMap.containsKey(user.getId()));
          }
        }

        if (room.getInvitedUsers() != null) {
          for (User user : room.getInvitedUsers()) {
            user.setIsOnline(onlineMap.containsKey(user.getId()));
          }
        }

        return room;
      });
}
