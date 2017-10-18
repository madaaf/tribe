package com.tribe.app.data.repository.user;

import android.util.Pair;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.Installation;
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
import java.util.ArrayList;
import java.util.List;
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
    this.searchResultRealmDataMapper =
        new SearchResultRealmDataMapper(userRealmDataMapper.getShortcutRealmDataMapper());
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

    return Observable.combineLatest(userDataStore.userInfos(null), userDataStore.inviteMap(),
        (userRealm, inviteMap) -> {
          User user = userRealmDataMapper.transform(userRealm);

          if (user != null && user.getShortcutList() != null) {
            if (inviteMap != null && inviteMap.size() > 0) {
              for (Shortcut shortcut : user.getShortcutList()) {
                for (Invite invite : inviteMap.values()) {
                  if (invite.getShortcut() != null && invite.getShortcut().getId().equals(shortcut.getId())) {
                    shortcut.setLive(true);
                  }
                }
              }
            }
          }

          user.setInviteList(inviteMap.values());

          return user;
        });
  }

  @Override public Observable<Shortcut> getShortcuts(String shortcutId) {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return userDataStore.shortcuts()
        .map(shortcutRealmList -> userRealmDataMapper.getShortcutRealmDataMapper()
            .transform(shortcutRealmList))
        .map(list -> {
          Shortcut shortcut = null;
          for (Shortcut sc : list) {
            if (sc.getId().equals(shortcutId)) shortcut = sc;
          }
          return shortcut;
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

  @Override public Observable<Void> removeShortcut(String shortcutId) {
    return null;
  }

  @Override public Observable<List<User>> getUsersInfosList(List<String> usersIds) {
    return null;
  }

  @Override public Observable<List<Shortcut>> singleShortcuts() {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return userDataStore.singleShortcuts()
        .map(listShortcuts -> userRealmDataMapper.getShortcutRealmDataMapper()
            .transform(listShortcuts));
  }

  @Override public Observable<List<Shortcut>> shortcuts() {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return userDataStore.shortcuts()
        .map(listShortcuts -> userRealmDataMapper.getShortcutRealmDataMapper()
            .transform(listShortcuts));
  }

  @Override public Observable<Shortcut> shortcutForUserIds(String... userIds) {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return userDataStore.shortcutForUserIds(userIds)
        .map(shortcutRealm -> userRealmDataMapper.getShortcutRealmDataMapper()
            .transform(shortcutRealm));
  }

  @Override public Observable<List<Shortcut>> blockedShortcuts() {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return userDataStore.blockedShortcuts()
        .map(blockedShortcuts -> userRealmDataMapper.getShortcutRealmDataMapper()
            .transform(blockedShortcuts));
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
    return Observable.combineLatest(userDataStore.userInfos(null),
        userDataStore.contactsToInvite().startWith(new ArrayList<ContactInterface>()),
        (userRealm, contactInviteList) -> {
          List<ContactInterface> ciList = new ArrayList<>();

          ciList.addAll(contactInviteList);
          return ciList;
        })
        .map(collection -> contactRealmDataMapper.transform(
            new ArrayList<ContactInterface>(collection)));
  }

  @Override public Observable<List<Shortcut>> searchLocally(String s, Set<String> includedUserIds) {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();
    return userDataStore.singleShortcuts().map(shortcutRealmList -> {
      List<Shortcut> shortcutList =
          userRealmDataMapper.getShortcutRealmDataMapper().transform(shortcutRealmList);
      return shortcutList;
    });
  }

  @Override public Observable<SearchResult> findByUsername(String username) {
    final DiskUserDataStore userDataStore =
        (DiskUserDataStore) this.userDataStoreFactory.createDiskDataStore();

    return userDataStore.findByUsername(username)
        .map(searchResultRealm -> searchResultRealmDataMapper.transform(searchResultRealm));
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
