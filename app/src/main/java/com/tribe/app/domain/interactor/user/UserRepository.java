package com.tribe.app.domain.interactor.user;

/**
 * Created by tiago on 04/05/2016.
 */

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;

import java.util.List;

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
     * @param pinId The pinId for the call.
     */
    Observable<AccessToken> loginWithPhoneNumber(final String phoneNumber, final String code, final String pinId);

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

    Observable<Installation> removeInstall();

    /**
     * Get an {@link Observable} which will emit a {@link List <Message>} containing infos
     * about the tribes received and sent.
     */
    Observable<List<Message>> messages();

    /**
     * Get an {@link Observable} which will emit a {@link List <Message>} containing infos
     * about the messages (tribe / chat) received.
     */
    Observable<List<Message>> messagesReceived(String friendshipId);

    Observable<User> updateUser(String key, String value);

}
