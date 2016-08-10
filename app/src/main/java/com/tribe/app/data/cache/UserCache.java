package com.tribe.app.data.cache;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.UserRealm;

import javax.inject.Singleton;

import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton
public interface UserCache {

    public boolean isExpired();
    public boolean isCached(int userId);
    public void put(UserRealm userRealm);
    public void put(AccessToken accessToken);
    public void put(Installation installation);
    public Observable<UserRealm> userInfos(String userId);
    public UserRealm userInfosNoObs(String userId);
    public GroupRealm groupInfos(String groupId);
    public FriendshipRealm friendshipForUserId(String userId);
}
