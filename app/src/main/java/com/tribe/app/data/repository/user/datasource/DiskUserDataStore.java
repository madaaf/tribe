package com.tribe.app.data.repository.user.datasource;

import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.UserRealm;

import rx.Observable;

/**
 * {@link UserDataStore} implementation based on connections to the database.
 */
public class DiskUserDataStore implements UserDataStore {

    private final UserCache userCache;
    /**
     * Construct a {@link UserDataStore} based on the database.
     * @param userCache A {@link UserCache} to retrieve the data.
     */
    public DiskUserDataStore(UserCache userCache) {
        this.userCache = userCache;
    }

    @Override
    public Observable<PinRealm> requestCode(String phoneNumber) {
        //return this.userCache.;
        return null;
    }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(String phoneNumber, String code, String scope) {
        return null;
    }

    @Override
    public Observable<AccessToken> loginWithUsername(String username, String password) { return null; }

    @Override
    public Observable<UserRealm> userInfos(String userId) {
        return this.userCache.userInfos(userId);
    }

    @Override
    public Observable<Installation> createOrUpdateInstall(String token) {
        return null;
    }
}
