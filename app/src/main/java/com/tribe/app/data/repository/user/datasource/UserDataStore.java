package com.tribe.app.data.repository.user.datasource;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;

import java.util.List;

import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface UserDataStore {

    /**
     * Get an {@link Observable} which will emit a {@link com.tribe.app.domain.entity.Pin} containing info about the code.
     *
     * @param phoneNumber The phoneNumber used to login.
     */
    Observable<PinRealm> requestCode(final String phoneNumber);

    /**
     * Get an {@link Observable} which will emit an Access Token.
     * @param phoneNumber The phoneNumber used to login.
     * @param code the code the user entered.
     * @param pinId the pinId for the call
     */
    Observable<AccessToken> loginWithPhoneNumber(String phoneNumber, String code, String pinId);

    /**
     * Get an {@link Observable} which will emit an Access Token.
     * @param username The username used to login.
     * @param password the password the user entered.
     */
    Observable<AccessToken> loginWithUsername(String username, String password);

    /**
     * Get an {@link Observable} which will emit a {@link UserRealm}
     * @param userId the id of the user for which we get the info
     *
     */
    Observable<UserRealm> userInfos(final String userId);

    /**
     * Get an {@link Observable} which will emit a {@link User}
     * @param token the token of the user for which we get the info
     *
     */
    Observable<Installation> createOrUpdateInstall(final String token);

    /**
     * Get an {@link Observable} which will emit a {@link List <MessageRealmInterface>} containing infos
     * about the messages received and sent.
     */
    Observable<List<MessageRealmInterface>> messages();
}
