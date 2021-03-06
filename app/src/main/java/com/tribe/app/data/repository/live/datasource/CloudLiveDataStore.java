package com.tribe.app.data.repository.live.datasource;

import android.content.Context;
import android.util.Pair;
import com.tribe.app.R;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.RemoveMessageEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import rx.Observable;

public class CloudLiveDataStore implements LiveDataStore {

  private final TribeApi tribeApi;
  private final Context context;
  private LiveCache liveCache;
  private UserCache userCache;
  private final AccessToken accessToken;

  public CloudLiveDataStore(Context context, AccessToken accessToken, TribeApi tribeApi,
      LiveCache liveCache, UserCache userCache) {
    this.context = context;
    this.tribeApi = tribeApi;
    this.liveCache = liveCache;
    this.userCache = userCache;
    this.accessToken = accessToken;
  }

  @Override public Observable<Room> getRoom(Live live) {
    String body;

    if (!StringUtils.isEmpty(live.getLinkId())) {
      body = context.getString(R.string.getRoom_linkId, live.getLinkId());
    } else {
      body = context.getString(R.string.getRoom_roomId, live.getRoomId());
    }

    final String request = context.getString(R.string.query, body) +
        "\n" +
        context.getString(R.string.roomFragment_infos) +
        "\n" +
        context.getString(R.string.userfragment_infos_light);

    return this.tribeApi.room(request)
        .doOnNext(room -> liveCache.putRoom(room))
        .compose(onlineLiveTransformer);
  }

  @Override public Observable<Room> getRoomLight(String roomId) {
    String body = context.getString(R.string.getRoom_roomId, roomId);

    final String request = context.getString(R.string.query, body) +
        "\n" +
        context.getString(R.string.roomFragment_infos_light) +
        "\n" +
        context.getString(R.string.userfragment_infos_light);

    return this.tribeApi.room(request).compose(onlineLiveTransformer);
  }

  @Override public Observable<Room> createRoom(String name, String gameId) {
    String params = "";

    if (!StringUtils.isEmpty(name)) {
      params += context.getString(R.string.createRoom_name, name);
    }

    if (!StringUtils.isEmpty(gameId)) {
      params += context.getString(R.string.createRoom_gameId, gameId);
    }

    String body = context.getString(R.string.createRoom, params);

    final String request = context.getString(R.string.mutation, body) +
        "\n" +
        context.getString(R.string.roomFragment_infos) +
        "\n" +
        context.getString(R.string.userfragment_infos_light);

    return this.tribeApi.createRoom(request)
        .doOnNext(room -> liveCache.putRoom(room))
        .compose(onlineLiveTransformer);
  }

  @Override public Observable<Room> updateRoom(String roomId, List<Pair<String, String>> values) {
    StringBuilder roomInputBuilder = new StringBuilder();

    for (Pair<String, String> value : values) {
      if (value.first.equals(Room.ACCEPT_RANDOM)) {
        roomInputBuilder.append(value.first + ": " + Boolean.valueOf(value.second));
        roomInputBuilder.append(",");
      } else if (value.first.equals(Room.NAME)) {
        roomInputBuilder.append(value.first + ": " + value.second);
        roomInputBuilder.append(",");
      } else if (value.first.equals(Room.GAME_ID)) {
        if (!StringUtils.isEmpty(value.second)) {
          roomInputBuilder.append(value.first + ": \"" + value.second + "\"");
          roomInputBuilder.append(",");
        } else {
          roomInputBuilder.append(value.first + ": " + value.second);
          roomInputBuilder.append(",");
        }
      }
    }

    String roomInput =
        roomInputBuilder.length() > 0 ? roomInputBuilder.substring(0, roomInputBuilder.length() - 1)
            : "";

    final String request = context.getString(R.string.mutation,
        context.getString(R.string.updateRoom, roomId, roomInput)) +
        "\n" +
        context.getString(R.string.roomFragment_infos) +
        "\n" +
        context.getString(R.string.userfragment_infos_light);

    return this.tribeApi.updateRoom(request).compose(onlineLiveTransformer);
  }

  @Override public Observable<Void> deleteRoom(String roomId) {
    if (StringUtils.isEmpty(roomId)) return Observable.just(null);

    final String request =
        context.getString(R.string.mutation, context.getString(R.string.removeRoom, roomId));

    return this.tribeApi.removeRoom(request)
        .doOnNext(aBoolean -> liveCache.removeRoom(roomId))
        .map(aBoolean -> null);
  }

  @Override
  public Observable<Boolean> createInvite(String roomId, boolean isAsking, String... userIds) {
    StringBuffer buffer = new StringBuffer();

    if (userIds.length > 0) {
      for (String id : userIds) {
        if (!id.equals(accessToken.getUserId())) {
          buffer.append(context.getString(R.string.createInvite,
              UUID.randomUUID().toString().replace("-", ""), roomId, id, isAsking));
        }
      }
    }

    String createInviteReqs = buffer.toString();

    if (StringUtils.isEmpty(createInviteReqs)) return Observable.just(false);

    final String request = context.getString(R.string.mutation, createInviteReqs);
    return this.tribeApi.createInvite(request);
  }

  @Override public Observable<Boolean> removeInvite(String roomId, String userId) {
    final String request = context.getString(R.string.mutation,
        context.getString(R.string.removeInvite, roomId, userId));

    return this.tribeApi.removeInvite(request);
  }

  @Override public Observable<Boolean> declineInvite(String roomId) {
    final String request =
        context.getString(R.string.mutation, context.getString(R.string.declineInvite, roomId));

    return this.tribeApi.declineInvite(request);
  }

  @Override public Observable<Boolean> buzzRoom(String roomId) {
    final String request =
        context.getString(R.string.mutation, context.getString(R.string.buzzRoom, roomId));

    return this.tribeApi.buzzRoom(request);
  }

  @Override public Observable<RemoveMessageEntity> removeMessage(String messageId) {
    final String request =
        context.getString(R.string.mutation, context.getString(R.string.removeMessage, messageId));

    return this.tribeApi.removeMessage(request);
  }

  @Override public Observable<String> randomRoomAssigned() {
    return null;
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
