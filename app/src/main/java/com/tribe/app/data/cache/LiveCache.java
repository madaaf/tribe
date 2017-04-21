package com.tribe.app.data.cache;

import com.tribe.app.domain.entity.Invite;
import java.util.Map;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton public interface LiveCache {

  Observable<Map<String, Boolean>> onlineMap();

  Observable<Map<String, Boolean>> liveMap();

  void putOnline(String id);

  void removeOnline(String id);

  void putLive(String id);

  void removeLive(String id);

  void putInvite(Invite invite);

  void removeInvite(Invite invite);

  void removeInviteFromRoomId(String roomId);

  Observable<Map<String, Invite>> inviteMap();

  Map<String, Invite> getInviteMap();
}
