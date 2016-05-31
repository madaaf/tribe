package com.tribe.app.data.repository.user.datasource;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;

import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface UserDataStore {
    /**
     * Get an {@link Observable} which will emit an Access Token.
     * @param username The username used to login.
     * @param password the password the user entered.
     */
    Observable<AccessToken> loginWithUsername(String username, String password);

    /**
     * Get an {@link Observable} which will emit a {@link User}
     * @param userId the id of the user for which we get the info
     *
     */
    Observable<UserRealm> getUserInfos(final String userId);
}
