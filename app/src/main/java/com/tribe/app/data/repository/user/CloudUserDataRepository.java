package com.tribe.app.data.repository.user;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.friendship.FriendshipRepository;
import com.tribe.app.domain.interactor.user.UserRepository;

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

    /**
     * Constructs a {@link FriendshipRepository}.
     *
     * @param dataStoreFactory A factory to construct different data source implementations.
     * @param realmDataMapper {@link UserRealmDataMapper}.
     */
    @Inject
    public CloudUserDataRepository(UserDataStoreFactory dataStoreFactory,
                                   UserRealmDataMapper realmDataMapper) {
        this.userDataStoreFactory = dataStoreFactory;
        this.userRealmDataMapper = realmDataMapper;
    }

    @Override
    public Observable<User> requestCode(String phoneNumber) {
        return null;
    }

    @Override
    public Observable<User> loginWithPhoneNumber(String phoneNumber, String code) {
        return null;
    }

    @Override
    public Observable<AccessToken> loginWithUserName(String username, String password) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.loginWithUsername(username, password);
    }

    @Override
    public Observable<User> getUserInfos(String userId) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.getUserInfos(userId)
                .map(userRealm -> this.userRealmDataMapper.transform(userRealm));
    }
}
