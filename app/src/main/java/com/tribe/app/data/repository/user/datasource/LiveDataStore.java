package com.tribe.app.data.repository.user.datasource;

import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import java.util.Map;
import rx.Observable;

/**
 * Created by tiago on 27/01/2017.
 */

public interface LiveDataStore {

  Observable<Map<String, Boolean>> onlineMap();

  Observable<Map<String, Boolean>> liveMap();

  Observable<Map<String, Invite>> inviteMap();

  Observable<User> getFbIdUpdated();

  Observable<Room> getRoomUpdated();
}
