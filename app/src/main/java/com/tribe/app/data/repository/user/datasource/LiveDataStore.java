package com.tribe.app.data.repository.user.datasource;

import java.util.Map;

import rx.Observable;

/**
 * Created by tiago on 27/01/2017.
 */

public interface LiveDataStore {

    Observable<Map<String, Boolean>> onlineMap();

    Observable<Map<String, Boolean>> liveMap();
}
