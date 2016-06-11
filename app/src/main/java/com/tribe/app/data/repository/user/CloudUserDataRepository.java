package com.tribe.app.data.repository.user;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.mapper.PinRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
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
    private final PinRealmDataMapper pinRealmDataMapper;

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
                                   PinRealmDataMapper pinRealmDataMapper) {
        this.userDataStoreFactory = dataStoreFactory;
        this.userRealmDataMapper = realmDataMapper;
        this.pinRealmDataMapper = pinRealmDataMapper;
    }

    @Override
    public Observable<Pin> requestCode(String phoneNumber) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.requestCode(phoneNumber).map(pin -> pinRealmDataMapper.transform(pin));
    }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(String phoneNumber, String code, String scope) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.loginWithPhoneNumber(phoneNumber, code, scope);
    }

    @Override
    public Observable<AccessToken> loginWithUserName(String username, String password) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.loginWithUsername(username, password);
    }

    @Override
    public Observable<User> userInfos(String userId) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createCloudDataStore();
        return userDataStore.userInfos(userId)
                .map(userRealm -> this.userRealmDataMapper.transform(userRealm));
    }
}
