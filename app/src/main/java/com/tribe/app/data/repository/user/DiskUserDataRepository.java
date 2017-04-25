package com.tribe.app.data.repository.user;

import android.util.Pair;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.ContactRealmDataMapper;
import com.tribe.app.data.realm.mapper.MembershipRealmDataMapper;
import com.tribe.app.data.realm.mapper.SearchResultRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.user.datasource.DiskUserDataStore;
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
import com.tribe.app.presentation.utils.StringUtils;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

/**
 * {@link DiskUserDataRepository} for retrieving user data.
 */
@Singleton public class DiskUserDataRepository implements UserRepository {

  private final UserDataStoreFactory userDataStoreFactory;
  private final UserRealmDataMapper userRealmDataMapper;
  private final ContactRealmDataMapper contactRealmDataMapper;
  private final SearchResultRealmDataMapper searchResultRealmDataMapper;
  private final MembershipRealmDataMapper membershipRealmDataMapper;

  /**
   * Constructs a {@link UserRepository}.
   *
   * @param dataStoreFactory A factory to construct different data source implementations.
   * @param realmDataMapper {@link UserRealmDataMapper}.
   */
  @Inject public DiskUserDataRepository(UserDataStoreFactory dataStoreFactory,
      UserRealmDataMapper realmDataMapper, ContactRealmDataMapper contactRealmDataMapper,
      MembershipRealmDataMapper membershipRealmDataMapper) {
    this.userDataStoreFactory = dataStoreFactory;
    this.userRealmDataMapper = realmDataMapper;
    this.contactRealmDataMapper = contactRealmDataMapper;
    this.searchResultRealmDataMapper =
        new SearchResultRealmDataMapper(userRealmDataMapper.getFriendshipRealmDataMapper());
    this.membershipRealmDataMapper = membershipRealmDataMapper;
  }

  @Override public Observable<Pin> requestCode(String phoneNumber) {
    return null;
  }

  @Override public Observable<AccessToken> loginWithPhoneNumber(LoginEntity loginEntity) {
    return null;
  }

  @Override public Observable<AccessToken> register(String displayName, String username,
      LoginEntity loginEntity) {
    return null;
  }

  @Override public Observable<User> userInfos(String userId) {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return Observable.combineLatest(userDataStore.userInfos(null),
        userDataStore.onlineMap().startWith(new HashMap<>()),
        userDataStore.liveMap().startWith(new HashMap<>()), userDataStore.inviteMap(),
        (userRealm, onlineMap, liveMap, inviteMap) -> {
          if (userRealm != null && userRealm.getFriendships() != null) {
            userRealm.setFriendships(
                updateOnlineLiveFriendship(userRealm.getFriendships(), onlineMap, liveMap, true));
            for (MembershipRealm membershipRealm : userRealm.getMemberships()) {
              membershipRealm.getGroup().setIsLive(liveMap.containsKey(membershipRealm.getSubId()));
            }
          }

          User user = userRealmDataMapper.transform(userRealm, true);
          if (user != null && inviteMap != null) user.setInviteList(inviteMap.values());

          return user;
        });
  }

  @Override public Observable<List<User>> getUsersInfosList(List<String> usersIds) {
    return null;
  }

  @Override public Observable<List<Friendship>> friendships() {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return Observable.combineLatest(userDataStore.friendships(),
        userDataStore.onlineMap().startWith(new HashMap<>()),
        userDataStore.liveMap().startWith(new HashMap<>()),
        (friendships, onlineMap, liveMap) -> userRealmDataMapper.getFriendshipRealmDataMapper()
            .transform(updateOnlineLiveFriendship(friendships, onlineMap, liveMap, true)));
  }

  private RealmList<FriendshipRealm> updateOnlineLiveFriendship(List<FriendshipRealm> friendships,
      Map<String, Boolean> onlineMap, Map<String, Boolean> liveMap, boolean excludeBlocked) {
    RealmList<FriendshipRealm> result = new RealmList<>();

    for (FriendshipRealm fr : friendships) {
      if (!excludeBlocked || (!StringUtils.isEmpty(fr.getStatus()) && fr.getStatus()
          .equals(FriendshipRealm.DEFAULT))) {
        fr.getFriend().setIsOnline(onlineMap.containsKey(fr.getSubId()));
        fr.setLive(liveMap.containsKey(fr.getId()));

        result.add(fr);
      }
    }
    return result;
  }

  @Override public Observable<User> updateUser(List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<Installation> createOrUpdateInstall(String token) {
    return null;
  }

  @Override public Observable<Installation> removeInstall() {
    return null;
  }

  @Override public Observable<List<Contact>> contacts() {
    final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
    return userDataStore.contacts()
        .map(collection -> contactRealmDataMapper.transform(
            new ArrayList<ContactInterface>(collection)));
  }

  @Override public Observable<List<Contact>> contactsFB() {
    final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
    return userDataStore.contactsFB()
        .map(collection -> contactRealmDataMapper.transform(
            new ArrayList<ContactInterface>(collection)));
  }

  @Override public Observable<List<Contact>> contactsOnApp() {
    final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
    return userDataStore.contactsOnApp()
        .map(collection -> contactRealmDataMapper.transform(
            new ArrayList<ContactInterface>(collection)));
  }

  @Override public Observable<List<Contact>> contactsInvite() {
    final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
    return Observable.combineLatest(userDataStore.userInfos(null), userDataStore.contactsOnApp(),
        userDataStore.contactsToInvite(), (userRealm, contactOnAppList, contactInviteList) -> {
          List<ContactInterface> ciList = new ArrayList<>();

          if (userRealm.getFriendships() != null && userRealm.getFriendships().size() > 0) {
            for (ContactInterface ciAB : contactOnAppList) {
              boolean add = true;

              for (FriendshipRealm fr : userRealm.getFriendships()) {
                for (UserRealm user : ciAB.getUsers()) {
                  if (fr.getFriend().equals(user)) add = false;
                }
              }

              if (add) ciList.add(ciAB);
            }
          }

          ciList.addAll(contactInviteList);
          return ciList;
        })
        .map(collection -> contactRealmDataMapper.transform(
            new ArrayList<ContactInterface>(collection)));
  }

  @Override public Observable<List<Object>> searchLocally(String s) {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();
    return Observable.combineLatest(userDataStore.userInfos(null)
            .map(userRealm -> userRealmDataMapper.transform(userRealm, true)),
        userDataStore.contactsOnApp()
            .map(contactInterfaces -> contactRealmDataMapper.transform(contactInterfaces)),
        userDataStore.contactsToInvite()
            .map(contactInterfaces -> contactRealmDataMapper.transform(contactInterfaces)),
        userDataStore.liveMap(), userDataStore.onlineMap(),
        (user, contactOnAppList, contactInviteList, liveMap, onlineMap) -> {
          List<Object> result = new ArrayList<>();
          Map<String, User> mapUsersAdded = new HashMap<>();

          for (Recipient recipient : user.getFriendshipList()) {
            if (recipient instanceof Friendship) {
              Friendship fr = (Friendship) recipient;
              mapUsersAdded.put(fr.getSubId(), fr.getFriend());
              fr.getFriend().setIsOnline(onlineMap.containsKey(fr.getSubId()));
              fr.setIsLive(liveMap.containsKey(fr.getId()));
            } else if (recipient instanceof Membership) {
              Membership me = (Membership) recipient;
              me.getGroup().setIsLive(liveMap.containsKey(me.getSubId()));
            }

            result.add(recipient);
          }

          for (Contact contact : contactOnAppList) {
            compute(mapUsersAdded, contact, result);
          }

          for (Contact contact : contactInviteList) {
            compute(mapUsersAdded, contact, result);
          }

          return result;
        });
  }

  private void compute(Map<String, User> mapUsersAdded, Contact contact, List<Object> result) {
    boolean shouldAdd = true;
    if (contact.getUserList() != null) {
      for (User userInList : contact.getUserList()) {
        if (mapUsersAdded.containsKey(userInList.getId())) {
          shouldAdd = false;
        }
      }
    }

    if (shouldAdd) result.add(contact);
  }

  @Override public Observable<SearchResult> findByUsername(String username) {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return Observable.combineLatest(userDataStore.findByUsername(username)
            .map(searchResultRealm -> searchResultRealmDataMapper.transform(searchResultRealm)),
        userDataStore.liveMap(), userDataStore.onlineMap(), (searchResult, liveMap, onlineMap) -> {
          if (searchResult != null && searchResult.getFriendship() != null) {
            Friendship fr = searchResult.getFriendship();
            fr.setIsLive(liveMap.containsKey(fr.getId()));
            fr.getFriend().setIsOnline(onlineMap.containsKey(fr.getSubId()));
          }

          return searchResult;
        });
  }

  @Override public Observable<Boolean> lookupUsername(String username) {
    return null;
  }

  @Override public Observable<List<Contact>> findByValue(String value) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
    return userDataStore.findByValue(value)
        .map(collection -> contactRealmDataMapper.transform(
            new ArrayList<ContactInterface>(collection)));
  }

  // TODO: update info in DB
  @Override public Observable<Friendship> createFriendship(String userId) {
    return null;
  }

  @Override public Observable<Void> createFriendships(String... userIds) {
    return null;
  }

  @Override public Observable<Void> removeFriendship(String userId) {
    return null;
  }

  @Override public Observable<Void> notifyFBFriends() {
    return null;
  }

  @Override public Observable<Group> getGroupMembers(String groupId) {
    return null;
  }

  @Override public Observable<Group> getGroupInfos(String groupId) {
    return null;
  }

  @Override public Observable<Membership> getMembershipInfos(String membershipId) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
    return userDataStore.getMembershipInfos(membershipId)
        .map(membershipRealm -> membershipRealmDataMapper.transform(membershipRealm));
  }

  public Observable<Membership> createGroup(GroupEntity groupEntity) {
    return null;
  }

  @Override
  public Observable<Group> updateGroup(String groupId, List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<Membership> updateMembership(String membershipId,
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

  @Override public Observable<Friendship> updateFriendship(String friendshipId,
      List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<List<Friendship>> getBlockedFriendshipList() {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return Observable.combineLatest(userDataStore.userInfos(null),
        userDataStore.onlineMap().startWith(new HashMap<>()),
        userDataStore.liveMap().startWith(new HashMap<>()), userDataStore.inviteMap(),
        (userRealm, onlineMap, liveMap, inviteMap) -> {
          RealmList<FriendshipRealm> result = new RealmList<>();

          for (FriendshipRealm fr : userRealm.getFriendships()) {
            if (!StringUtils.isEmpty(fr.getStatus()) && !fr.getStatus()
                .equals(FriendshipRealm.DEFAULT)) {
              result.add(fr);
            }
          }

          userRealm.setFriendships(updateOnlineLiveFriendship(result, onlineMap, liveMap, false));

          return userRealmDataMapper.transform(userRealm, true).getFriendships();
        });
  }

  @Override public Observable<String> getHeadDeepLink(String url) {
    return null;
  }

  @Override public Observable<Membership> createMembership(String groupId) {
    return null;
  }

  @Override public Observable<Recipient> getRecipientInfos(String recipientId, boolean isToGroup) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();

    return userDataStore.getRecipientInfos(recipientId, isToGroup).map(recipientRealmInterface -> {
      if (recipientRealmInterface instanceof MembershipRealm) {
        return userRealmDataMapper.getMembershipRealmDataMapper()
            .transform((MembershipRealm) recipientRealmInterface);
      } else {
        return userRealmDataMapper.getFriendshipRealmDataMapper()
            .transform((FriendshipRealm) recipientRealmInterface);
      }
    });
  }

  @Override
  public Observable<RoomConfiguration> joinRoom(String id, boolean isGroup, String roomId, String linkId) {
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

  @Override public Observable<Void> sendInvitations() { return null; }

  @Override public Observable<String> getRoomLink(String roomId) {
    return null;
  }
}
