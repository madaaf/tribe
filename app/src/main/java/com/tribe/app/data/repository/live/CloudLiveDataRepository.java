package com.tribe.app.data.repository.live;

import android.util.Pair;
import com.tribe.app.data.repository.live.datasource.CloudLiveDataStore;
import com.tribe.app.data.repository.live.datasource.LiveDataStoreFactory;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.interactor.live.LiveRepository;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class CloudLiveDataRepository implements LiveRepository {

  private final LiveDataStoreFactory dataStoreFactory;

  @Inject public CloudLiveDataRepository(LiveDataStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }

  @Override public Observable<Room> getRoom(Live live) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.getRoom(live);
  }

  @Override public Observable<Room> createRoom(String name, String... userIds) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.createRoom(name, userIds);
  }

  @Override public Observable<Room> updateRoom(String roomId, List<Pair<String, String>> pairList) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.updateRoom(roomId, pairList);
  }

  @Override public Observable<Void> deleteRoom(String roomId) {
    return null;
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

  @Override public Observable<String> randomRoomAssigned() {
    return null;
  }

  @Override public Observable<Room> getRoomUpdated() {
    return null;
  }
}
