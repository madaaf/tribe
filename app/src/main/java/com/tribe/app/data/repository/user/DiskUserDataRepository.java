package com.tribe.app.data.repository.user;

import com.tribe.app.data.realm.AccessToken;
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
 * {@link DiskUserDataRepository} for retrieving user data.
 */
@Singleton
public class DiskUserDataRepository implements UserRepository {

    private final UserDataStoreFactory userDataStoreFactory;
    private final UserRealmDataMapper userRealmDataMapper;

    /**
     * Constructs a {@link UserRepository}.
     *
     * @param dataStoreFactory A factory to construct different data source implementations.
     * @param realmDataMapper {@link UserRealmDataMapper}.
     */
    @Inject
    public DiskUserDataRepository(UserDataStoreFactory dataStoreFactory,
                                  UserRealmDataMapper realmDataMapper) {
        this.userDataStoreFactory = dataStoreFactory;
        this.userRealmDataMapper = realmDataMapper;
    }

    @Override
    public Observable<Pin> requestCode(String phoneNumber) { return null; }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(String phoneNumber, String code, String scope) { return null; }

    @Override
    public Observable<AccessToken> loginWithUserName(String username, String password) { return null; }

    @Override
    public Observable<User> userInfos(String userId) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
        return userDataStore.userInfos(userId)
                .map(userRealm -> this.userRealmDataMapper.transform(userRealm));
    }
}
