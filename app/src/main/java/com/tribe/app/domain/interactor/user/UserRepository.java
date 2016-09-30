package com.tribe.app.domain.interactor.user;

/**
 * Created by tiago on 04/05/2016.
 */

import android.util.Pair;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.SearchResult;
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
     * @param loginEntity infos needed to login.
     */
    Observable<AccessToken> loginWithPhoneNumber(final LoginEntity loginEntity);

    /**
     * Get an {@link Observable} which will emit an Access Token.
     * @param displayName the full name of the user.
     * @param username the username of the user.
     * @param loginEntity the login infos needed to register (pinId, etc)
     */
    Observable<AccessToken> register(String displayName, String username, LoginEntity loginEntity);


    /**
     * Get an {@link Observable} which will emit a {@link User}
     * @param userId the id of the user for which we get the info
     * @param filterRecipient the filter for the recipients
     *
     */
    Observable<User> userInfos(final String userId, String filterRecipient);

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

    /**
     * Get an {@link Observable} which will emit a {@link User} containing infos
     * about the user updated.
     */
    Observable<User> updateUser(List<Pair<String, String>> values);

    /**
     * Get an {@link Observable} which will emit a {@link List <Contact>} containing infos
     * about the contacts from all sources (AddressBook / Facebook).
     */
    Observable<List<Contact>> contacts();

    /**
     * Get an {@link Observable} which will emit
     */
    Observable<Void> howManyFriends();

    /**
     * Get an {@link Observable} which will emit a {@link SearchResult} containing infos
     * about the user searched.
     */
    Observable<SearchResult> findByUsername(String username);

    /**
     * Get an {@link Observable} which will emit a {@link Boolean} saying whether or not the
     * username is taken
     */
    Observable<Boolean> lookupUsername(String username);

    /**
     * Get an {@link Observable} which will emit a {@link com.tribe.app.domain.entity.Friendship} containing infos
     * about the contact.
     */
    Observable<Friendship> createFriendship(String userId);

    /**
     * Get an {@link Observable} which will emit a void
     */
    Observable<Void> removeFriendship(String userId);

    /**
     * Get an {@link Observable} which will emit a {@link Contact} containing infos
     * about the contact.
     */
    Observable<List<Contact>> findByValue(String value);

    /**
     * Get an {@link Observable} which will emit nothing
     */
    Observable<Void> notifyFBFriends();

    Observable<Group> getGroupMembers(String groupId);

    Observable<Group> createGroup(String groupName, List<String> memberIds, boolean isPrivate, String pictureUri);

    Observable<Group> updateGroup(String groupId, String groupName, String pictureUri);

    Observable<Void> addMembersToGroup(String groupId, List<String> memberIds);

    Observable<Void> removeMembersFromGroup(String groupId, List<String> memberIds);

    Observable<Void> addAdminsToGroup(String groupId, List<String> memberIds);

    Observable<Void> removeAdminsFromGroup(String groupId, List<String> memberIds);

    Observable<Void> removeGroup(String groupId);

    Observable<Void> leaveGroup(String membershipId);

    Observable<Void> bootstrapSupport();
}
