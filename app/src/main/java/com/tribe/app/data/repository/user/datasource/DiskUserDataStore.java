package com.tribe.app.data.repository.user.datasource;

import android.util.Pair;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.entity.LinkIdResult;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.RecipientRealmInterface;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import rx.Observable;

/**
 * {@link UserDataStore} implementation based on connections to the database.
 */
public class DiskUserDataStore implements UserDataStore, LiveDataStore {

  private final UserCache userCache;
  private final LiveCache liveCache;
  private final ContactCache contactCache;
  private final AccessToken accessToken;

  /**
   * Construct a {@link UserDataStore} based on the database.
   *
   * @param userCache A {@link UserCache} to retrieve the data.
   * @param accessToken A {@link AccessToken} that contains the current user id.
   * @param contactCache A {@link ContactCache} that contains the cached data of the user's
   * possible
   * contacts
   */
  public DiskUserDataStore(UserCache userCache, LiveCache liveCache, AccessToken accessToken,
      ContactCache contactCache) {
    this.userCache = userCache;
    this.liveCache = liveCache;
    this.accessToken = accessToken;
    this.contactCache = contactCache;
  }

  @Override public Observable<PinRealm> requestCode(String phoneNumber, boolean shouldCall) {
    return null;
  }

  @Override public Observable<AccessToken> login(LoginEntity loginEntity) {
    return null;
  }

  @Override public Observable<AccessToken> register(String displayName, String username,
      LoginEntity loginEntity) {
    return null;
  }

  @Override public Observable<UserRealm> userInfos(String userId) {
    return this.userCache.userInfos(accessToken.getUserId());
  }

  @Override public Observable<List<UserRealm>> userInfosList(List<String> userIds) {
    return null;
  }

  @Override public Observable<List<FriendshipRealm>> friendships() {
    return this.userCache.friendships();
  }

  @Override public Observable<Installation> createOrUpdateInstall(String token) {
    return null;
  }

  @Override public Observable<Installation> removeInstall() {
    return null;
  }

  @Override public Observable<UserRealm> updateUser(List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<LinkIdResult> updateUserFacebook(String accessToken) {
    return null;
  }

  @Override
  public Observable<LinkIdResult> updateUserPhoneNumber(String accessToken, String phoneNumber) {
    return null;
  }

  @Override public Observable<Void> incrUserTimeInCall(String userId, Long timeInCall) {
    return null;
  }

  @Override public Observable<List<ContactInterface>> contacts() {
    return contactCache.contacts().map(contactABRealms -> new ArrayList<>(contactABRealms));
  }

  @Override public Observable<List<ContactInterface>> contactsFB() {
    return contactCache.contactsFB().map(contactFBRealms -> new ArrayList<>(contactFBRealms));
  }

  @Override public Observable<List<ContactInterface>> contactsOnApp() {
    return contactCache.contactsOnApp();
  }

  @Override public Observable<List<ContactInterface>> contactsToInvite() {
    return contactCache.contactsToInvite()
        .map(contactABRealms -> new ArrayList<ContactInterface>(contactABRealms));
  }

  @Override public Observable<SearchResultRealm> findByUsername(String username) {
    return contactCache.findContactByUsername(username);
  }

  @Override public Observable<Boolean> lookupUsername(String username) {
    return null;
  }

  @Override public Observable<List<ContactABRealm>> findByValue(String username) {
    return contactCache.findContactsByValue(username);
  }

  @Override public Observable<FriendshipRealm> createFriendship(String userId) {
    return null;
  }

  @Override public Observable<Void> removeFriendship(String userId) {
    return null;
  }

  @Override public Observable<Void> notifyFBFriends() {
    return null;
  }

  @Override public Observable<GroupRealm> getGroupMembers(String groupId) {
    return null;
  }

  @Override public Observable<GroupRealm> getGroupInfos(String groupId) {
    return null;
  }

  @Override public Observable<MembershipRealm> getMembershipInfos(String membershipId) {
    return Observable.just(userCache.membershipInfos(membershipId));
  }

  @Override public Observable<MembershipRealm> createGroup(GroupEntity groupEntity) {
    return null;
  }

  @Override
  public Observable<GroupRealm> updateGroup(String groupId, List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<MembershipRealm> updateMembership(String membershipId,
      List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<Void> addMembersToGroup(String groupId, List<String> memberIds) {
    return null;
  }

  @Override public Observable<Void> removeMembersFromGroup(String groupId, List<String> memberIds) {
    return null;
  }

  @Override public Observable<Void> removeGroup(String groupId) {
    return null;
  }

  @Override public Observable<Void> leaveGroup(String groupId) {
    return null;
  }

  @Override public Observable<FriendshipRealm> updateFriendship(String friendshipId,
      List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<String> getHeadDeepLink(String url) {
    return null;
  }

  @Override public Observable<MembershipRealm> createMembership(String groupId) {
    return null;
  }

  @Override public Observable<RecipientRealmInterface> getRecipientInfos(String recipientId,
      boolean isToGroup) {
    return Observable.just(isToGroup ? userCache.membershipForGroupId(recipientId)
        : userCache.friendshipForUserId(recipientId));
  }

  @Override public Observable<Map<String, Boolean>> onlineMap() {
    return liveCache.onlineMap();
  }

  @Override public Observable<Map<String, Boolean>> liveMap() {
    return liveCache.liveMap();
  }

  @Override public Observable<String> callRouletteMap() {
    return liveCache.getRandomRoomAssignedValue();
  }

  @Override public Observable<User> getFbIdUpdated() {
    return liveCache.getFbIdUpdated();
  }

  @Override public Observable<RoomConfiguration> joinRoom(String id, boolean isGroup, String roomId,
      String linkId) {
    return null;
  }

  @Override public Observable<Boolean> inviteUserToRoom(String roomId, String userId) {
    return null;
  }

  @Override public Observable<Boolean> buzzRoom(String roomId) {
    return null;
  }

  @Override public Observable<Void> declineInvite(String roomId) {
    return null;
  }

  @Override public Observable<Void> sendInvitations() {
    return null;
  }

  @Override public Observable<String> getRoomLink(String roomId) {
    return null;
  }

  @Override public Observable<Boolean> bookRoomLink(String linkId) {
    return null;
  }

  @Override public Observable<Void> roomAcceptRandom(String roomId) {
    return null;
  }

  @Override public Observable<Boolean> reportUser(String userId) {
    return null;
  }

  @Override public Observable<Map<String, Invite>> inviteMap() {
    return liveCache.inviteMap();
  }
}
