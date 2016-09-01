package com.tribe.app.data.repository.user.datasource;

import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.UserRealm;

import java.util.List;

import rx.Observable;

/**
 * {@link UserDataStore} implementation based on connections to the database.
 */
public class DiskUserDataStore implements UserDataStore {

    private final UserCache userCache;
    private final AccessToken accessToken;

    /**
     * Construct a {@link UserDataStore} based on the database.
     * @param userCache A {@link UserCache} to retrieve the data.
     * @param accessToken A {@link AccessToken} that contains the current user id.
     */
    public DiskUserDataStore(UserCache userCache, AccessToken accessToken) {
        this.userCache = userCache;
        this.accessToken = accessToken;
    }

    @Override
    public Observable<PinRealm> requestCode(String phoneNumber) {
        return null;
    }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(String phoneNumber, String code, String pinId) {
        return null;
    }

    @Override
    public Observable<AccessToken> loginWithUsername(String username, String password) { return null; }

    @Override
    public Observable<UserRealm> userInfos(String userId) {
        return this.userCache.userInfos(accessToken.getUserId());
    }

    @Override
    public Observable<Installation> createOrUpdateInstall(String token) {
        return null;
    }

    /***
     * NOT USED
     * @return
     */
    @Override
    public Observable<List<MessageRealmInterface>> messages() {
        return null;
    }

    @Override
    public Observable<UserRealm> setUsername(String username) {
        return null;

    }
}
