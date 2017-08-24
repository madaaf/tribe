package com.tribe.app.data.repository.live.datasource;

import android.content.Context;
import com.tribe.app.R;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.BookRoomLinkEntity;
import com.tribe.app.data.repository.game.datasource.GameDataStore;
import com.tribe.app.domain.entity.Room;
import java.util.List;
import rx.Observable;

public class CloudLiveDataStore implements LiveDataStore {

  private final TribeApi tribeApi;
  private final Context context;
  private final LiveCache liveCache;

  public CloudLiveDataStore(Context context, TribeApi tribeApi, LiveCache liveCache) {
    this.context = context;
    this.tribeApi = tribeApi;
    this.liveCache = liveCache;
  }

  @Override public Observable<Room> getRoom(String roomId) {
    String body = context.getString(R.string.getRoom, roomId);

    final String request = context.getString(R.string.query, body) +
        "\n" +
        context.getString(R.string.roomFragment_infos) +
        "\n" +
        context.getString(R.string.userfragment_infos_light);

    return this.tribeApi.room(request);
  }

  @Override public Observable<Room> createRoom(String name, String[] userIds) {
    return null;
  }

  @Override public Observable<Void> deleteRoom(String roomId) {
    return null;
  }

  @Override public Observable<Boolean> inviteUserToRoom(String roomId, String userId) {
    final String request = context.getString(R.string.mutation,
        context.getString(R.string.inviteToRoom, roomId, userId));

    return this.tribeApi.inviteUserToRoom(request);
  }

  @Override public Observable<Boolean> buzzRoom(String roomId) {
    final String request =
        context.getString(R.string.mutation, context.getString(R.string.buzzRoom, roomId));

    return this.tribeApi.buzzRoom(request);
  }

  @Override public Observable<Void> declineInvite(String roomId) {
    liveCache.removeInviteFromRoomId(roomId);

    final String request =
        context.getString(R.string.mutation, context.getString(R.string.declineInvite, roomId));

    return this.tribeApi.declineInvite(request);
  }

  @Override public Observable<String> getRoomLink(String roomId) {
    final String request =
        context.getString(R.string.mutation, context.getString(R.string.getRoomLink, roomId));

    return this.tribeApi.getRoomLink(request)
        .map(roomLinkEntity -> roomLinkEntity != null ? roomLinkEntity.getLink() : null);
  }

  @Override public Observable<Boolean> bookRoomLink(String linkId) {
    final String request =
        context.getString(R.string.mutation, context.getString(R.string.bookRoomLink, linkId));

    return this.tribeApi.bookRoomLink(request).map(BookRoomLinkEntity::isRoomBooked);
  }

  @Override public Observable<Void> roomAcceptRandom(String roomId) {
    final String request =
        context.getString(R.string.mutation, context.getString(R.string.roomAcceptRandom, roomId));

    return this.tribeApi.roomAcceptRandom(request);
  }

  @Override public Observable<String> randomRoomAssigned() {
    return null;
  }
}
