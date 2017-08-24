package com.tribe.app.data.repository.live;

import com.tribe.app.data.repository.live.datasource.CloudLiveDataStore;
import com.tribe.app.data.repository.live.datasource.LiveDataStoreFactory;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.interactor.live.LiveRepository;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

@Singleton public class CloudLiveDataRepository implements LiveRepository {

  private final LiveDataStoreFactory dataStoreFactory;

  @Inject public CloudLiveDataRepository(LiveDataStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }

  @Override public Observable<Room> getRoom(String roomId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.getRoom(roomId);
  }

  @Override public Observable<Room> createRoom(String name, String... userIds) {
    return null;
  }

  @Override public Observable<Void> deleteRoom(String roomId) {
    return null;
  }

  @Override public Observable<Boolean> inviteUserToRoom(String roomId, String userId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.inviteUserToRoom(roomId, userId);
  }

  @Override public Observable<Boolean> buzzRoom(String roomId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.buzzRoom(roomId);
  }

  @Override public Observable<Void> declineInvite(String roomId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.declineInvite(roomId);
  }

  @Override public Observable<String> getRoomLink(String roomId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.getRoomLink(roomId);
  }

  @Override public Observable<Boolean> bookRoomLink(String linkId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.bookRoomLink(linkId);
  }

  @Override public Observable<Void> roomAcceptRandom(String roomId) {
    final CloudLiveDataStore cloudDataStore =
        (CloudLiveDataStore) this.dataStoreFactory.createCloudDataStore();
    return cloudDataStore.roomAcceptRandom(roomId);
  }

  @Override public Observable<String> randomRoomAssigned() {
    return null;
  }
}
