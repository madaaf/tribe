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

  @Inject public LiveCacheImpl(Context context) {
    this.context = context;
    onlineMap = new ObservableRxHashMap<>();
    liveMap = new ObservableRxHashMap<>();
  }

  @Override public Observable<Map<String, Boolean>> onlineMap() {
    return onlineMap.getMapObservable().startWith(onlineMap.getMap());
  }

  @Override public Observable<Map<String, Boolean>> liveMap() {
    return liveMap.getMapObservable().startWith(liveMap.getMap());
  }

  @Override public void putOnlineMap(Map<String, Boolean> onlineMap) {
    this.onlineMap.putAll(onlineMap);
  }

  @Override public void putLiveMap(Map<String, Boolean> liveMap) {
    this.liveMap.putAll(liveMap);
  }

  @Override public void putInvites(Map<String, Invite> inviteList) {

  }
}
