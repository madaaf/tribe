package com.tribe.app.domain.interactor.user;

/**
 * Created by tiago on 04/05/2016.
 */

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;

import rx.Observable;

/**
 * Interface that represents a Repository for getting {@link User} related data.
 */
public interface UserRepository {

    /**
     * Get an {@link Observable} which will emit a {@link com.tribe.app.domain.entity.Pin} containing info about the code.
     *
     * @param phoneNumber The phoneNumber used to login.
     */
    Observable<Pin> requestCode(final String phoneNumber);

    /**
     * Get an {@link Observable} which will emit a {@link User}.
     *
     * @param phoneNumber The phoneNumber used to login.
     * @param code the validation code the user entered.
     * @param scope The scope for the call.
     */
    Observable<AccessToken> loginWithPhoneNumber(final String phoneNumber, final String code, final String scope);

    /**
     * Get an {@link Observable} which will emit a {@link User}.
     *
     * @param username The username used to login.
     * @param password the password the user entered.
     */
    Observable<AccessToken> loginWithUserName(final String username, final String password);


    /**
     * Get an {@link Observable} which will emit a {@link User}
     * @param userId the id of the user for which we get the info
     *
     */
    Observable<User> userInfos(final String userId);

    /**
     * Get an {@link Observable} which will emit a {@link User}
     * @param token the token of the user for which we get the info
     *
     */
    Observable<Installation> createOrUpdateInstall(final String token);
}
