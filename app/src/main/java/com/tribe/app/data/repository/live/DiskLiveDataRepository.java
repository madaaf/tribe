package com.tribe.app.data.repository.live;

import android.util.Pair;
import com.tribe.app.data.repository.live.datasource.DiskLiveDataStore;
import com.tribe.app.data.repository.live.datasource.LiveDataStoreFactory;
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

  @Inject public DiskLiveDataRepository(LiveDataStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }

  @Override public Observable<String> randomRoomAssigned() {
    final DiskLiveDataStore liveDataStore =
        (DiskLiveDataStore) this.dataStoreFactory.createDiskDataStore();
    return liveDataStore.randomRoomAssigned();
  }

  @Override public Observable<Room> getRoomUpdated() {
    final DiskLiveDataStore liveDataStore =
        (DiskLiveDataStore) this.dataStoreFactory.createDiskDataStore();
    return liveDataStore.getRoomUpdated();
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

  @Override public Observable<Boolean> inviteUserToRoom(String roomId, String userId) {
    return null;
  }

  @Override public Observable<Boolean> dismissInvite(String roomId, String userId) {
    return null;
  }

  @Override public Observable<Boolean> buzzRoom(String roomId) {
    return null;
  }

  @Override public Observable<Void> declineInvite(String roomId) {
    return null;
  }

  @Override public Observable<Boolean> bookRoomLink(String linkId) {
    return null;
  }
}
