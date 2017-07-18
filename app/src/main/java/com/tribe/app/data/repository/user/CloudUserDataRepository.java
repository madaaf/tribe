package com.tribe.app.data.repository.user;

import android.util.Pair;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.mapper.ContactRealmDataMapper;
import com.tribe.app.data.realm.mapper.GroupRealmDataMapper;
import com.tribe.app.data.realm.mapper.MembershipRealmDataMapper;
import com.tribe.app.data.realm.mapper.PinRealmDataMapper;
import com.tribe.app.data.realm.mapper.SearchResultRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.user.datasource.CloudUserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupEntity;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

/**
 * {@link CloudUserDataRepository} for retrieving user data.
 */
@Singleton public class CloudUserDataRepository implements UserRepository {

  private final UserDataStoreFactory userDataStoreFactory;
  private final UserRealmDataMapper userRealmDataMapper;
  private final PinRealmDataMapper pinRealmDataMapper;
  private final ContactRealmDataMapper contactRealmDataMapper;
  private final SearchResultRealmDataMapper searchResultRealmDataMapper;
  private final GroupRealmDataMapper groupRealmDataMapper;
  private final MembershipRealmDataMapper membershipRealmDataMapper;

  /**
   * Constructs a {@link UserRepository}.
   *
   * @param dataStoreFactory A factory to construct different data source implementations.
   * @param realmDataMapper {@link UserRealmDataMapper}.
   * @param pinRealmDataMapper {@link PinRealmDataMapper}.
   */
  @Inject public CloudUserDataRepository(UserDataStoreFactory dataStoreFactory,
      UserRealmDataMapper realmDataMapper, PinRealmDataMapper pinRealmDataMapper,
      ContactRealmDataMapper contactRealmDataMapper, GroupRealmDataMapper groupRealmDataMapper,
      MembershipRealmDataMapper membershipRealmDataMapper) {
    this.userDataStoreFactory = dataStoreFactory;
    this.userRealmDataMapper = realmDataMapper;
    this.pinRealmDataMapper = pinRealmDataMapper;
    this.contactRealmDataMapper = contactRealmDataMapper;
    this.searchResultRealmDataMapper =
        new SearchResultRealmDataMapper(userRealmDataMapper.getFriendshipRealmDataMapper());
    this.groupRealmDataMapper = groupRealmDataMapper;
    this.membershipRealmDataMapper = membershipRealmDataMapper;
  }

  @Override public Observable<Pin> requestCode(String phoneNumber, boolean shouldCall) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.requestCode(phoneNumber, shouldCall)
        .map(pin -> pinRealmDataMapper.transform(pin));
  }

  @Override public Observable<AccessToken> loginWithPhoneNumber(LoginEntity loginEntity) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.loginWithPhoneNumber(loginEntity);
  }

  @Override public Observable<AccessToken> register(String displayName, String username,
      LoginEntity loginEntity) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.register(displayName, username, loginEntity);
  }

  @Override public Observable<User> userInfos(String userId) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.userInfos(userId).doOnError(throwable -> {
      throwable.printStackTrace();
    }).map(userRealm -> this.userRealmDataMapper.transform(userRealm, true));
  }

  @Override public Observable<List<User>> getUsersInfosList(List<String> userIds) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.userInfosList(userIds).doOnError(throwable -> {
      throwable.printStackTrace();
    }).map(userRealm -> this.userRealmDataMapper.transform(userRealm, false));
  }

  @Override public Observable<List<Friendship>> friendships() {
    return null;
  }

  @Override public Observable<Installation> createOrUpdateInstall(String token) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.createOrUpdateInstall(token);
  }

  @Override public Observable<Installation> removeInstall() {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.removeInstall();
  }

  @Override public Observable<User> updateUser(List<Pair<String, String>> values) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.updateUser(values)
        .map(userRealm -> this.userRealmDataMapper.transform(userRealm, true));
  }

  @Override public Observable<List<Contact>> contacts() {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.contacts()
        .map(collection -> this.contactRealmDataMapper.transform(
            new ArrayList<ContactInterface>(collection)));
  }

  @Override public Observable<List<Contact>> contactsFB() {
    return null;
  }

  @Override public Observable<List<Contact>> contactsOnApp() {
    return null;
  }

  @Override public Observable<List<Contact>> contactsInvite() {
    return null;
  }

  @Override public Observable<List<Object>> searchLocally(String s) {
    return null;
  }

  @Override public Observable<SearchResult> findByUsername(String username) {
    final UserDataStore cloudDataStore = this.userDataStoreFactory.createCloudDataStore();

    return cloudDataStore.findByUsername(username)
        .map(searchResultRealm -> this.searchResultRealmDataMapper.transform(searchResultRealm));
  }

  @Override public Observable<Boolean> lookupUsername(String username) {
    final UserDataStore cloudDataStore = this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.lookupUsername(username);
  }

  @Override public Observable<List<Contact>> findByValue(String value) {
    return null;
  }

  @Override public Observable<Friendship> createFriendship(String userId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.createFriendship(userId).map(friendshipRealm -> {
      if (friendshipRealm != null) {
        return this.userRealmDataMapper.getFriendshipRealmDataMapper().transform(friendshipRealm);
      } else {
        return null;
      }
    });
  }

  @Override public Observable<Void> createFriendships(String... userIds) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.createFriendships(userIds);
  }

  @Override public Observable<Void> removeFriendship(String friendshipId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.removeFriendship(friendshipId);
  }

  @Override public Observable<Void> notifyFBFriends() {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.notifyFBFriends();
  }

  @Override public Observable<Group> getGroupMembers(String groupId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.getGroupMembers(groupId).map(this.groupRealmDataMapper::transform);
  }

  @Override public Observable<Group> getGroupInfos(String groupId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.getGroupInfos(groupId).map(this.groupRealmDataMapper::transform);
  }

  @Override public Observable<Membership> getMembershipInfos(String membershipId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.getMembershipInfos(membershipId)
        .map(this.membershipRealmDataMapper::transform);
  }

  @Override public Observable<Membership> createGroup(GroupEntity groupEntity) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.createGroup(groupEntity)
        .map((membershipRealm) -> this.membershipRealmDataMapper.transform(membershipRealm));
  }

  @Override
  public Observable<Group> updateGroup(String groupId, List<Pair<String, String>> values) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.updateGroup(groupId, values).map(this.groupRealmDataMapper::transform);
  }

  @Override public Observable<Membership> updateMembership(String membershipId,
      List<Pair<String, String>> values) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.updateMembership(membershipId, values)
        .map(this.membershipRealmDataMapper::transform);
  }

  @Override public Observable<Void> addMembersToGroup(String groupId, List<String> memberIds) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.addMembersToGroup(groupId, memberIds);
  }

  @Override public Observable<Void> removeMembersFromGroup(String groupId, List<String> memberIds) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.removeMembersFromGroup(groupId, memberIds);
  }

  @Override public Observable<Void> removeGroup(String groupId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.removeGroup(groupId);
  }

  @Override public Observable<Void> leaveGroup(String membershipId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.leaveGroup(membershipId);
  }

  @Override public Observable<Friendship> updateFriendship(String friendshipId,
      List<Pair<String, String>> values) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.updateFriendship(friendshipId, values)
        .map(this.userRealmDataMapper.getFriendshipRealmDataMapper()::transform);
  }

  @Override public Observable<List<Friendship>> getBlockedFriendshipList() {
    return null;
  }

  @Override public Observable<String> getHeadDeepLink(String url) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.getHeadDeepLink(url);
  }

  @Override public Observable<Membership> createMembership(String groupId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.createMembership(groupId)
        .map(membershipRealm -> userRealmDataMapper.getMembershipRealmDataMapper()
            .transform(membershipRealm));
  }

  @Override public Observable<Recipient> getRecipientInfos(String recipientId, boolean isToGroup) {
    return null;
  }

  @Override public Observable<RoomConfiguration> joinRoom(String id, boolean isGroup, String roomId,
      String linkId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.joinRoom(id, isGroup, roomId, linkId);
  }

  @Override public Observable<Boolean> inviteUserToRoom(String roomId, String userId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.inviteUserToRoom(roomId, userId);
  }

  @Override public Observable<Boolean> buzzRoom(String roomId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.buzzRoom(roomId);
  }

  @Override public Observable<Void> declineInvite(String roomId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.declineInvite(roomId);
  }

  @Override public Observable<Void> sendInvitations() {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.sendInvitations();
  }

  @Override public Observable<String> getRoomLink(String roomId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.getRoomLink(roomId);
  }

  @Override public Observable<Boolean> bookRoomLink(String linkId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.bookRoomLink(linkId);
  }

  @Override public Observable<Void> roomAcceptRandom(String roomId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.roomAcceptRandom(roomId);
  }

  @Override public Observable<String> randomRoomAssigned() {
    return null;
  }

  @Override public Observable<User> getFbIdUpdated() {
    return null;
  }

  @Override public Observable<Boolean> reportUser(String userId) {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.reportUser(userId);
  }

  @Override public Observable<List<Friendship>> unblockedFriendships() {
    return null;
  }
}
