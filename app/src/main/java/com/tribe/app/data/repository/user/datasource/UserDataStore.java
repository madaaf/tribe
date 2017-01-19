package com.tribe.app.data.repository.user.datasource;

import android.util.Pair;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.RecipientRealmInterface;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.GroupEntity;
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
     *
     */
    Observable<UserRealm> userInfos(final String userId);

    /**
     * Get an {@link Observable} which will emit a {@link List<Friendship>}
     *
     */
    Observable<List<FriendshipRealm>> friendships();

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
     * Get an {@link Observable} which will emit a {@link List <ContactInterface>} containing infos
     * about the contacts from Facebook.
     */
    Observable<List<ContactInterface>> contactsFB();

    Observable<List<ContactInterface>> contactsOnApp();

    Observable<List<ContactInterface>> contactsToInvite();

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

    Observable<GroupRealm> getGroupInfos(String groupId);

    Observable<MembershipRealm> getMembershipInfos(String membershipId);

    Observable<MembershipRealm> createGroup(GroupEntity groupEntity);

    Observable<GroupRealm> updateGroup(String groupId, List<Pair<String, String>> values);

    Observable<MembershipRealm> updateMembership(String membershipId, List<Pair<String, String>> values);

    Observable<Void> addMembersToGroup(String groupId, List<String> memberIds);

    Observable<Void> removeMembersFromGroup(String groupId, List<String> memberIds);

    Observable<Void> removeGroup(String groupId);

    Observable<Void> leaveGroup(String membershipId);

    Observable<Void> bootstrapSupport();

    Observable<FriendshipRealm> updateFriendship(String friendshipId, @FriendshipRealm.FriendshipStatus String status);

    Observable<String> getHeadDeepLink(String url);

    Observable<MembershipRealm> createMembership(String groupId);

    Observable<RecipientRealmInterface> getRecipientInfos(String recipientId, boolean isToGroup);

    Observable<Void> sendOnlineNotification();
}
