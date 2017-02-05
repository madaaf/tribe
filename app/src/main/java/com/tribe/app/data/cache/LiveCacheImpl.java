package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.domain.entity.Invite;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;

import java.util.Map;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 01/27/2017.
 */
public class LiveCacheImpl implements LiveCache {

  private Context context;
  private ObservableRxHashMap<String, Boolean> onlineMap;
  private ObservableRxHashMap<String, Boolean> liveMap;
  private ObservableRxHashMap<String, Invite> inviteMap;

  @Inject public LiveCacheImpl(Context context) {
    this.context = context;
    onlineMap = new ObservableRxHashMap<>();
    liveMap = new ObservableRxHashMap<>();
    inviteMap = new ObservableRxHashMap<>();
  }

  @Override public Observable<Map<String, Boolean>> onlineMap() {
    return onlineMap.getMapObservable().startWith(onlineMap.getMap());
  }

  @Override public Observable<Map<String, Boolean>> liveMap() {
    return liveMap.getMapObservable().startWith(liveMap.getMap());
  }

  @Override public void putOnline(String id) {
    onlineMap.put(id, true);
  }

  @Override public void removeOnline(String id) {
    onlineMap.remove(id);
  }

  @Override public void putLive(String id) {
    liveMap.put(id, true);
  }

  @Override public void removeLive(String id) {
    liveMap.remove(id);
  }

  @Override public void putInvite(Invite invite) {
    this.inviteMap.put(invite.getRoomId(), invite);
  }

  @Override public void removeInvite(Invite invite) {
    this.inviteMap.remove(invite.getRoomId());
  }

  @Override public Observable<Map<String, Invite>> inviteMap() {
    return inviteMap.getMapObservable().startWith(inviteMap.getMap());
  }

  @Override public Map<String, Invite> getInviteMap() {
    return inviteMap.getMap();
  }
}
