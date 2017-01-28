package com.tribe.app.data.cache;

import java.util.Map;

import javax.inject.Singleton;

import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton
public interface LiveCache {

    Observable<Map<String, Boolean>> onlineMap();

    Observable<Map<String, Boolean>> liveMap();

    void putOnlineMap(Map<String, Boolean> onlineMap);

    void putLiveMap(Map<String, Boolean> liveMap);
}
