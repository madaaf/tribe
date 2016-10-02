package com.tribe.app.data.cache;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.MoreType;
import com.tribe.app.presentation.view.utils.ScoreUtils;

import java.util.List;

import javax.inject.Singleton;

import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton
public interface UserCache {

    boolean isExpired();
    boolean isCached(int userId);
    void put(UserRealm userRealm);
    void put(AccessToken accessToken);
    void put(Installation installation);
    Observable<UserRealm> userInfos(String userId);
    UserRealm userInfosNoObs(String userId);
    MembershipRealm membershipForGroupId(String groupId);
    FriendshipRealm friendshipForUserId(String userId);
    void removeFriendship(String friendshipId);
    void insertGroup(GroupRealm groupRealm);
    void updateGroup(String groupId, String groupName, String pictureUri);
    void addMembersToGroup(String groupId, List<String> memberIds);
    void removeMembersFromGroup(String groupId, List<String> memberIds);
    void addAdminsToGroup(String groupId, List<String> memberIds);
    void removeAdminsFromGroup(String groupId, List<String> memberIds);
    void removeGroup(String groupId);
    void removeGroupFromMembership(String membershipId);
    void insertMembership(String userId, MembershipRealm membershipRealm);
    void updateScore(String userId, ScoreUtils.Point point);
    Observable<FriendshipRealm> updateFriendship(String friendshipId, MoreType moreType);
    FriendshipRealm updateFriendshipNoObs(String friendshipId, MoreType moreType);
}
