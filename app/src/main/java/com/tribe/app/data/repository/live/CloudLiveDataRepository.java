package com.tribe.app.data.repository.live;

import android.util.Pair;
import com.tribe.app.data.network.entity.RemoveMessageEntity;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.live.datasource.CloudLiveDataStore;
import com.tribe.app.data.repository.live.datasource.LiveDataStoreFactory;
import com.tribe.app.data.repository.user.datasource.DiskUserDataStore;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.interactor.live.LiveRepository;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class CloudLiveDataRepository implements LiveRepository {

  private final LiveDataStoreFactory dataStoreFactory;
  private UserRealmDataMapper userRealmDataMapper;
  private DiskUserDataStore diskUserDataStore = null;

  @Inject public CloudLiveDataRepository(LiveDataStoreFactory dataStoreFactory,
      UserRealmDataMapper userRealmDataMapper) {
    this.dataStoreFactory = dataStoreFactory;
    this.diskUserDataStore = dataStoreFactory.createDiskUserDataStore();
    this.userRealmDataMapper = userRealmDataMapper;
  }

  @Override public Observable<Room> getRoom(Live live) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.getRoom(live).compose(roomWithShortcutTransformer);
  }

  @Override public Observable<Room> createRoom(String name, String... userIds) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.createRoom(name, userIds);
  }

  @Override public Observable<Room> updateRoom(String roomId, List<Pair<String, String>> pairList) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.updateRoom(roomId, pairList).compose(roomWithShortcutTransformer);
  }

  @Override public Observable<Void> deleteRoom(String roomId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.deleteRoom(roomId);
  }

  @Override public Observable<Boolean> createInvite(String roomId, String... userIds) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.createInvite(roomId, userIds);
  }

  @Override public Observable<Boolean> removeInvite(String roomId, String userId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.removeInvite(roomId, userId);
  }

  @Override public Observable<Boolean> declineInvite(String roomId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.declineInvite(roomId);
  }

  @Override public Observable<Boolean> buzzRoom(String roomId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.buzzRoom(roomId);
  }

  @Override public Observable<RemoveMessageEntity> removeMessage(String messageId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.removeMessage(messageId);
  }

  @Override public Observable<String> randomRoomAssigned() {
    return null;
  }

  @Override public Observable<Room> getRoomUpdated(String roomId) {
    return null;
  }

  private Observable.Transformer<Room, Room> roomWithShortcutTransformer =
      roomObservable -> roomObservable.flatMap(room -> {
        List<String> userIds = room.getUserIds();
        if (userIds.size() == 0) {
          return Observable.just(null);
        } else {
          return diskUserDataStore.shortcutForUserIdsNoObs(
              userIds.toArray(new String[userIds.size()]));
        }
      }, (room, shortcutRealm) -> {
        if (shortcutRealm != null) {
          room.setShortcut(
              userRealmDataMapper.getShortcutRealmDataMapper().transform(shortcutRealm));
        }

        return room;
      }).doOnError(Throwable::printStackTrace);
}
