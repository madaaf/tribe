package com.tribe.app.data.repository.user;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.mapper.ContactRealmDataMapper;
import com.tribe.app.data.realm.mapper.PinRealmDataMapper;
import com.tribe.app.data.realm.mapper.SearchResultRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.user.datasource.CloudUserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Pin;
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
@Singleton
public class CloudUserDataRepository implements UserRepository {

    private final UserDataStoreFactory userDataStoreFactory;
    private final UserRealmDataMapper userRealmDataMapper;
    private final PinRealmDataMapper pinRealmDataMapper;
    private final ContactRealmDataMapper contactRealmDataMapper;
    private final SearchResultRealmDataMapper searchResultRealmDataMapper;

    /**
     * Constructs a {@link UserRepository}.
     *
     * @param dataStoreFactory A factory to construct different data source implementations.
     * @param realmDataMapper {@link UserRealmDataMapper}.
     * @param pinRealmDataMapper {@link PinRealmDataMapper}.
     */
    @Inject
    public CloudUserDataRepository(UserDataStoreFactory dataStoreFactory,
                                   UserRealmDataMapper realmDataMapper,
                                   PinRealmDataMapper pinRealmDataMapper,
                                   ContactRealmDataMapper contactRealmDataMapper) {
        this.userDataStoreFactory = dataStoreFactory;
        this.userRealmDataMapper = realmDataMapper;
        this.pinRealmDataMapper = pinRealmDataMapper;
        this.contactRealmDataMapper = contactRealmDataMapper;
        this.searchResultRealmDataMapper = new SearchResultRealmDataMapper(userRealmDataMapper.getFriendshipRealmDataMapper());
    }

    @Override
    public Observable<Pin> requestCode(String phoneNumber) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.requestCode(phoneNumber).map(pin -> pinRealmDataMapper.transform(pin));
    }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(LoginEntity loginEntity) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.loginWithPhoneNumber(loginEntity);
    }

    @Override
    public Observable<AccessToken> register(String displayName, String username, LoginEntity loginEntity) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.register(displayName, username, loginEntity);
    }

    @Override
    public Observable<User> userInfos(String userId) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.userInfos(userId)
                .doOnError(throwable -> {
                    throwable.printStackTrace();
                })
                .map(userRealm -> this.userRealmDataMapper.transform(userRealm));
    }

    @Override
    public Observable<Installation> createOrUpdateInstall(String token) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.createOrUpdateInstall(token);
    }

    @Override
    public Observable<Installation> removeInstall() {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.removeInstall();
    }

    /***
     *
     * @return is not used as it's just for sync
     */
    @Override
    public Observable<List<Message>> messages() {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.messages().map(messageRealmList -> {
            List<Message> messageList = new ArrayList<Message>();
            return messageList;
        });
    }

    @Override
    public Observable<User> updateUser(String username, String displayName, String pictureUri) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.updateUser(username, displayName, pictureUri)
                .map(userRealm -> this.userRealmDataMapper.transform(userRealm));
    }

    /***
     *
     * NOT USED
     */
    @Override
    public Observable<List<Message>> messagesReceived(String friendshipId) {
        return null;
    }

    @Override
    public Observable<List<Contact>> contacts() {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.contacts().map(collection -> this.contactRealmDataMapper.transform(new ArrayList<>(collection)));
    }

    @Override
    public Observable<Void> howManyFriends() {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.howManyFriends();
    }

    @Override
    public Observable<SearchResult> findByUsername(String username) {
        final UserDataStore cloudDataStore = this.userDataStoreFactory.createCloudDataStore();

        return cloudDataStore.findByUsername(username)
                .map(searchResultRealm -> this.searchResultRealmDataMapper.transform(searchResultRealm));
    }

    /**
     * UNUSED
     */
    @Override
    public Observable<User> lookupUsername(String username) {
        return null;
    }

    @Override
    public Observable<List<Contact>> findByValue(String value) {
        return null;
    }

    @Override
    public Observable<Friendship> createFriendship(String userId) {
        final CloudUserDataStore cloudDataStore = (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
        return cloudDataStore
                .createFriendship(userId)
                .map(friendshipRealm -> {
                    if (friendshipRealm != null)
                        return this.userRealmDataMapper.getFriendshipRealmDataMapper().transform(friendshipRealm);
                    else
                        return null;
                });
    }

    @Override
    public Observable<Void> removeFriendship(String friendshipId) {
        final CloudUserDataStore cloudDataStore = (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
        return cloudDataStore.removeFriendship(friendshipId);
    }

    @Override
    public Observable<Void> notifyFBFriends() {
        final CloudUserDataStore cloudDataStore = (CloudUserDataStore) this.userDataStoreFactory.createCloudDataStore();
        return cloudDataStore.notifyFBFriends();
    }
}
