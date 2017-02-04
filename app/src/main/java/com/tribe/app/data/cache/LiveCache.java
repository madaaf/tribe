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

  void putOnlineMap(Map<String, Boolean> onlineMap);

  void putLiveMap(Map<String, Boolean> liveMap);

  void putInvites(Map<String, Invite> inviteMap);

  void putInvite(Invite invite);

  void removeInvite(Invite invite);

  Observable<Map<String, Invite>> inviteMap();

  Map<String, Invite> getInviteMap();
}
