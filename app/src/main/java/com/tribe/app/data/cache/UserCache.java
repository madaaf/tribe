package com.tribe.app.data.cache;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.UserRealm;

import java.util.List;

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
    public void removeFriendship(String friendshipId);
    public void createGroup(String userId, String groupId, String groupName, List<String> memberIds, Boolean isPrivate, String pictureUri);
    public void updateGroup(String groupId, String groupName, String pictureUri);
    public void addMembersToGroup(String groupId, List<String> memberIds);
    public void removeMembersFromGroup(String groupId, List<String> memberIds);
    public void addAdminsToGroup(String groupId, List<String> memberIds);
    public void removeAdminsFromGroup(String groupId, List<String> memberIds);
    public void removeGroup(String groupId);
}
