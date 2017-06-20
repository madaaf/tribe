package com.tribe.app.data.cache;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.UserRealm;
import java.util.List;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton public interface UserCache {

  boolean isExpired();

  boolean isCached(int userId);

  void put(UserRealm userRealm);

  void updateCurrentUser(UserRealm userRealm);

  void put(AccessToken accessToken);

  void put(Installation installation);

  Observable<UserRealm> userInfos(String userId);

  Observable<List<FriendshipRealm>> friendships();

  UserRealm userInfosNoObs(String userId);

  MembershipRealm membershipForGroupId(String groupId);

  FriendshipRealm friendshipForUserId(String userId);

  void removeFriendship(String friendshipId);

  void insertGroup(GroupRealm groupRealm);

  void updateGroup(GroupRealm groupRealm, boolean isFull);

  void addMembersToGroup(String groupId, List<String> memberIds);

  void removeMembersFromGroup(String groupId, List<String> memberIds);

  void removeGroup(String groupId);

  void removeGroupFromMembership(String membershipId);

  void insertMembership(String userId, MembershipRealm membershipRealm);

  void updateMembership(MembershipRealm membershipRealm);

  void updateFriendship(FriendshipRealm friendshipRealm);

  FriendshipRealm updateFriendshipNoObs(String friendshipId,
      @FriendshipRealm.FriendshipStatus String moreType);

  MembershipRealm membershipInfos(String membershipId);

  void updateUserRealmList(List<UserRealm> userRealm);

  void updateGroupRealmList(List<GroupRealm> groupRealmList);

  void addFriendship(FriendshipRealm friendshipRealm);

  void removeFriendship(FriendshipRealm friendshipRealm);

  void addMembership(MembershipRealm membershipRealm);

  void removeMembership(String id);

}
