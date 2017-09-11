package com.tribe.app.data.repository.user;

import android.util.Pair;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.mapper.ContactRealmDataMapper;
import com.tribe.app.data.realm.mapper.MessageRealmDataMapper;
import com.tribe.app.data.realm.mapper.PinRealmDataMapper;
import com.tribe.app.data.realm.mapper.SearchResultRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.user.datasource.CloudUserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.user.UserRepository;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.widget.chat.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
  private final MessageRealmDataMapper messageRealmDataMapper;
  private final DateUtils dateUtils;

  /**
   * Constructs a {@link UserRepository}.
   *
   * @param dataStoreFactory A factory to construct different data source implementations.
   * @param realmDataMapper {@link UserRealmDataMapper}.
   * @param pinRealmDataMapper {@link PinRealmDataMapper}.
   */
  @Inject public CloudUserDataRepository(UserDataStoreFactory dataStoreFactory,
      UserRealmDataMapper realmDataMapper, PinRealmDataMapper pinRealmDataMapper,
      ContactRealmDataMapper contactRealmDataMapper,
      MessageRealmDataMapper messageRealmDataMapper, DateUtils dateUtils) {
    this.userDataStoreFactory = dataStoreFactory;
    this.userRealmDataMapper = realmDataMapper;
    this.pinRealmDataMapper = pinRealmDataMapper;
    this.contactRealmDataMapper = contactRealmDataMapper;
    this.searchResultRealmDataMapper =
        new SearchResultRealmDataMapper(userRealmDataMapper.getFriendshipRealmDataMapper());
    this.messageRealmDataMapper = messageRealmDataMapper;
    this.dateUtils = dateUtils;
  }

  @Override public Observable<Pin> requestCode(String phoneNumber, boolean shouldCall) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.requestCode(phoneNumber, shouldCall)
        .map(pin -> pinRealmDataMapper.transform(pin));
  }

  @Override public Observable<AccessToken> login(LoginEntity loginEntity) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.login(loginEntity);
  }

  @Override public Observable<AccessToken> register(String displayName, String username,
      LoginEntity loginEntity) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.register(displayName, username, loginEntity);
  }

  @Override public Observable<User> userInfos(String userId) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.userInfos(userId)
        .doOnError(throwable -> throwable.printStackTrace())
        .map(userRealm -> this.userRealmDataMapper.transform(userRealm, true));
  }

  @Override public Observable<List<Message>> userMessageInfo(String[] userIds) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.userMessage(userIds).doOnError(throwable -> {
      throwable.printStackTrace();
    }).map(userRealm -> {
      List<Message> messageList = new ArrayList<Message>();
      messageList = this.userRealmDataMapper.transform(userRealm, false).getMessages();
      return messageList;
    });
  }

  @Override public Observable<Message> createMessage(String[] userIds, String data) {
    String date = dateUtils.getUTCDateAsString();
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.createMessage(userIds, data, date).doOnError(throwable -> {
      throwable.printStackTrace();
    }).map(this.messageRealmDataMapper::transform);
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

  @Override public Observable<User> updateUserFacebook(String userId, String accessToken) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.updateUserFacebook(accessToken)
        .flatMap(aVoid -> userDataStore.userInfos(userId)
            .map(userRealm -> this.userRealmDataMapper.transform(userRealm, true)));
  }

  @Override public Observable<User> updateUserPhoneNumber(String userId, String accessToken,
      String phoneNumber) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.updateUserPhoneNumber(accessToken, phoneNumber)
        .flatMap(aVoid -> userDataStore.userInfos(userId)
            .map(userRealm -> this.userRealmDataMapper.transform(userRealm, true)));
  }

  @Override public Observable<Void> incrUserTimeInCall(String userId, Long timeInCall) {
    final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
    return userDataStore.incrUserTimeInCall(userId, timeInCall);
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

  @Override public Observable<List<Object>> searchLocally(String s, Set<String> includedUserIds) {
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

  @Override public Observable<Recipient> getRecipientInfos(String recipientId) {
    return null;
  }

  @Override public Observable<Void> sendInvitations() {
    final CloudUserDataStore cloudDataStore =
        (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
    return cloudDataStore.sendInvitations();
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
