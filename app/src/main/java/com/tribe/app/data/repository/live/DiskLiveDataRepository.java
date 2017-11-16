package com.tribe.app.data.repository.live;

import android.util.Pair;
import com.tribe.app.data.network.entity.RemoveMessageEntity;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.live.datasource.DiskLiveDataStore;
import com.tribe.app.data.repository.live.datasource.LiveDataStoreFactory;
import com.tribe.app.data.repository.user.datasource.DiskUserDataStore;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.interactor.live.LiveRepository;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by tiago on 24/08/2017.
 */

public class DiskLiveDataRepository implements LiveRepository {

  private final LiveDataStoreFactory dataStoreFactory;
  private UserRealmDataMapper userRealmDataMapper;
  private DiskUserDataStore diskUserDataStore = null;

  @Inject public DiskLiveDataRepository(LiveDataStoreFactory dataStoreFactory,
      UserRealmDataMapper userRealmDataMapper) {
    this.dataStoreFactory = dataStoreFactory;
    this.diskUserDataStore = dataStoreFactory.createDiskUserDataStore();
    this.userRealmDataMapper = userRealmDataMapper;
  }

  @Override public Observable<String> randomRoomAssigned() {
    final DiskLiveDataStore liveDataStore =
        (DiskLiveDataStore) this.dataStoreFactory.createDiskDataStore();
    return liveDataStore.randomRoomAssigned();
  }

  @Override public Observable<Room> getRoomUpdated(String roomId) {
    final DiskLiveDataStore liveDataStore =
        (DiskLiveDataStore) this.dataStoreFactory.createDiskDataStore();
    return liveDataStore.getRoomUpdated(roomId).compose(roomWithShortcutTransformer);
  }

  @Override public Observable<Room> getRoom(Live live) {
    return null;
  }

  @Override public Observable<Room> createRoom(String name, String[] userIds) {
    return null;
  }

  @Override public Observable<Room> updateRoom(String roomId, List<Pair<String, String>> pairList) {
    return null;
  }

  @Override public Observable<Void> deleteRoom(String roomId) {
    return null;
  }

  @Override public Observable<Boolean> createInvite(String roomId, String userId) {
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

  private Observable.Transformer<Room, Room> roomWithShortcutTransformer =
      roomObservable -> roomObservable.flatMap(room -> {
        List<String> userIds = room.getUserIds();
        return diskUserDataStore.shortcutForUserIdsNoObs(
            userIds.toArray(new String[userIds.size()]));
      }, (room, shortcutRealm) -> {
        room.setShortcut(userRealmDataMapper.getShortcutRealmDataMapper().transform(shortcutRealm));
        return room;
      });
}
