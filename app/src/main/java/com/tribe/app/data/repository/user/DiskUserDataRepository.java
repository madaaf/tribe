package com.tribe.app.data.repository.user;

import android.util.Pair;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.mapper.ContactRealmDataMapper;
import com.tribe.app.data.realm.mapper.SearchResultRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.user.datasource.DiskUserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.user.UserRepository;
import com.tribe.app.presentation.utils.StringUtils;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  /**
   * Constructs a {@link UserRepository}.
   *
   * @param dataStoreFactory A factory to construct different data source implementations.
   * @param realmDataMapper {@link UserRealmDataMapper}.
   */
  @Inject public DiskUserDataRepository(UserDataStoreFactory dataStoreFactory,
      UserRealmDataMapper realmDataMapper, ContactRealmDataMapper contactRealmDataMapper) {
    this.userDataStoreFactory = dataStoreFactory;
    this.userRealmDataMapper = realmDataMapper;
    this.contactRealmDataMapper = contactRealmDataMapper;
    this.searchResultRealmDataMapper = new SearchResultRealmDataMapper(userRealmDataMapper.getShortcutRealmDataMapper());
  }

  @Override public Observable<Pin> requestCode(String phoneNumber, boolean shouldCall) {
    return null;
  }

  @Override public Observable<AccessToken> login(LoginEntity loginEntity) {
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
          if (userRealm != null && userRealm.getShortcuts() != null) {
            RealmList<ShortcutRealm> shortcutRealmList =
                updateOnlineLiveShortcuts(userRealm.getShortcuts(), onlineMap, true);

            if (inviteMap != null && inviteMap.size() > 0) {
              RealmList<ShortcutRealm> endShortcuts = new RealmList<>();
              for (ShortcutRealm shortcutRealm : shortcutRealmList) {
                for (Invite invite : inviteMap.values()) {
                  List<String> shortcutMembersIds = shortcutRealm.getMembersIds();
                  // we add the current user id because he's not included in the shortcut
                  shortcutMembersIds.add(userId);
                  if (!invite.isShortcut(shortcutMembersIds)) {
                    endShortcuts.add(shortcutRealm);
                  }
                }
              }

              userRealm.setShortcuts(endShortcuts);
            } else {
              userRealm.setShortcuts(shortcutRealmList);
            }
          }

          User user = userRealmDataMapper.transform(userRealm);
          user.setInviteList(inviteMap.values());

          return user;
        });
  }

  @Override public Observable<User> getFbIdUpdated() {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();
    return userDataStore.getFbIdUpdated();
  }

  @Override public Observable<Boolean> reportUser(String userId) {
    return null;
  }

  @Override public Observable<Shortcut> createShortcut(String... userIds) {
    return null;
  }

  @Override
  public Observable<Shortcut> updateShortcut(String shortcutId, List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<Boolean> removeShortcut(String shortcutId) {
    return null;
  }

  @Override public Observable<List<User>> getUsersInfosList(List<String> usersIds) {
    return null;
  }

  @Override public Observable<List<Shortcut>> shortcuts() {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return Observable.combineLatest(userDataStore.shortcuts(),
        userDataStore.onlineMap().startWith(new HashMap<>()),
        userDataStore.liveMap().startWith(new HashMap<>()),
        (shortcuts, onlineMap, liveMap) -> userRealmDataMapper.getShortcutRealmDataMapper()
            .transform(updateOnlineLiveShortcuts(shortcuts, onlineMap, true)));
  }

  private RealmList<ShortcutRealm> updateOnlineLiveShortcuts(List<ShortcutRealm> shortcutRealmList,
      Map<String, Boolean> onlineMap, boolean excludeBlocked) {
    RealmList<ShortcutRealm> result = new RealmList<>();

    for (ShortcutRealm st : shortcutRealmList) {
      if (!excludeBlocked ||
          (!StringUtils.isEmpty(st.getStatus()) && st.getStatus().equals(ShortcutRealm.DEFAULT))) {
        st.setOnline(onlineMap.containsKey(st.getId()) || st.isUniqueMemberOnline(onlineMap));
        result.add(st);
      }
    }
    return result;
  }

  @Override public Observable<User> updateUser(List<Pair<String, String>> values) {
    return null;
  }

  @Override public Observable<User> updateUserFacebook(String userId, String accessToken) {
    return null;
  }

  @Override public Observable<User> updateUserPhoneNumber(String userId, String accessToken,
      String phoneNumber) {
    return null;
  }

  @Override public Observable<Void> incrUserTimeInCall(String userId, Long timeInCall) {
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

          ciList.addAll(contactInviteList);
          return ciList;
        })
        .map(collection -> contactRealmDataMapper.transform(
            new ArrayList<ContactInterface>(collection)));
  }

  @Override public Observable<List<Object>> searchLocally(String s, Set<String> includedUserIds) {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();
    return Observable.combineLatest(
        userDataStore.userInfos(null).map(userRealm -> userRealmDataMapper.transform(userRealm)),
        userDataStore.contactsOnApp()
            .map(contactInterfaces -> contactRealmDataMapper.transform(contactInterfaces)),
        userDataStore.contactsToInvite()
            .map(contactInterfaces -> contactRealmDataMapper.transform(contactInterfaces)),
        userDataStore.liveMap(), userDataStore.onlineMap(),
        (user, contactOnAppList, contactInviteList, liveMap, onlineMap) -> {
          List<Object> result = new ArrayList<>();
          Map<String, User> mapUsersAdded = new HashMap<>();

          for (Contact contact : contactOnAppList) {
            compute(mapUsersAdded, includedUserIds, contact, result);
          }

          for (Contact contact : contactInviteList) {
            compute(mapUsersAdded, includedUserIds, contact, result);
          }

          return result;
        });
  }

  private void compute(Map<String, User> mapUsersAdded, Set<String> includedUserIds,
      Contact contact, List<Object> result) {
    boolean shouldAdd = true;
    if (contact.getUserList() != null) {
      for (User userInList : contact.getUserList()) {
        if (mapUsersAdded.containsKey(userInList.getId()) &&
            (includedUserIds == null || !includedUserIds.contains(userInList.getId()))) {
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

  @Override public Observable<Void> notifyFBFriends() {
    return null;
  }

  @Override public Observable<String> getHeadDeepLink(String url) {
    return null;
  }

  @Override public Observable<Recipient> getRecipientInfos(String recipientId) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();

    return userDataStore.getRecipientInfos(recipientId).map(recipientRealmInterface -> {
      return null;
    });
  }

  @Override public Observable<Void> sendInvitations() {
    return null;
  }
}
