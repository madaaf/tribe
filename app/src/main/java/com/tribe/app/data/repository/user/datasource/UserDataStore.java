package com.tribe.app.data.repository.user.datasource;

import android.util.Pair;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.SearchResultRealm;
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
     * @param loginEntity the infos for the log in
     */
    Observable<AccessToken> loginWithPhoneNumber(LoginEntity loginEntity);

    /**
     * Get an {@link Observable} which will emit an Access Token.
     * @param displayName the full name of the user.
     * @param username the username of the user.
     * @param loginEntity the login infos needed to register (pinId, etc)
     */
    Observable<AccessToken> register(String displayName, String username, LoginEntity loginEntity);

    /**
     * Get an {@link Observable} which will emit a {@link UserRealm}
     * @param userId the id of the user for which we get the info
     * @param filterRecipient the filter for the recipients
     *
     */
    Observable<UserRealm> userInfos(final String userId, String filterRecipient);

    /**
     * Get an {@link Observable} which will emit a {@link User}
     * @param token the token of the user for which we get the info
     *
     */
    Observable<Installation> createOrUpdateInstall(final String token);

    /**
     * Remove the install from the server
     * @return
     */
    Observable<Installation> removeInstall();

    /**
     * Get an {@link Observable} which will emit a {@link List <MessageRealmInterface>} containing infos
     * about the messages received and sent.
     */
    Observable<List<MessageRealmInterface>> messages();


    /**
     *
     * @param values
     * @return the new user value
     */
    Observable<UserRealm> updateUser(List<Pair<String, String>> values);

    /**
     * Get an {@link Observable} which will emit a {@link List <ContactInterface>} containing infos
     * about the contacts from address book.
     */
    Observable<List<ContactInterface>> contacts();

    /**
     * Get an {@link Observable} which will get the number of friends from each address book member
     */
    Observable<Void> howManyFriends();

    /**
     * Get an {@link Observable} which will emit a {@link SearchResultRealm} containing infos
     * about the search results.
     */
    Observable<SearchResultRealm> findByUsername(String username);

    /**
     * Get an {@link Observable} which will emit a {@link Boolean} saying whether or not the
     * username is taken
     */
    Observable<Boolean> lookupUsername(String username);

    /**
     * Get an {@link Observable} which will emit a {@link List<ContactInterface>} containing infos
     * about the contacts corresponding to the value.
     */
    Observable<List<ContactABRealm>> findByValue(String value);

    /**
     * Get an {@link Observable} which will emit a {@link com.tribe.app.data.realm.FriendshipRealm} containing infos
     * about the new friendship.
     */
    Observable<FriendshipRealm> createFriendship(String userId);

    /**
     * Get an {@link Observable} which will emit a void object
     */
    Observable<Void> removeFriendship(String userId);

    /**
     * Get an {@link Observable} which will emit nothing
     */
    Observable<Void> notifyFBFriends();

    /**
     * Get an {@link Observable} which will emit the members of the group
     */
    Observable<GroupRealm> getGroupMembers(String groupId);

    Observable<GroupRealm> createGroup(String groupName, List<String> memberIds, Boolean isPrivate, String pictureUri);

    Observable<GroupRealm> updateGroup(String groupId, String groupName, String pictureUri);

    Observable<Void> addMembersToGroup(String groupId, List<String> memberIds);

    Observable<Void> removeMembersFromGroup(String groupId, List<String> memberIds);

    Observable<Void> addAdminsToGroup(String groupId, List<String> memberIds);

    Observable<Void> removeAdminsFromGroup(String groupId, List<String> memberIds);

    Observable<Void> removeGroup(String groupId);

    Observable<Void> leaveGroup(String groupId);
}
