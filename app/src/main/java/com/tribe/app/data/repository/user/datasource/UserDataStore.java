package com.tribe.app.data.repository.user.datasource;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;

import java.util.List;

import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface UserDataStore {
    /**
     * Get an {@link Observable} which will emit a logged in User.
     * @param username The username used to login.
     * @param password the password the user entered.
     */
    Observable<UserRealm> loginWithUsername(String username, String password);

    /**
     * Get an {@link Observable} which will emit a {@link User}
     * @param userId the id of the user for which we get the info
     *
     */
    Observable<UserRealm> getUserInfos(final String userId);
}
