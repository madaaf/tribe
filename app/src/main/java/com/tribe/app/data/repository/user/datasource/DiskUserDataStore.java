package com.tribe.app.data.repository.user.datasource;

import android.util.Pair;

import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.RecipientRealmInterface;
import com.tribe.app.data.realm.SearchResultRealm;
import com.tribe.app.data.realm.UserRealm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rx.Observable;

/**
 * {@link UserDataStore} implementation based on connections to the database.
 */
public class DiskUserDataStore implements UserDataStore {

    private final UserCache userCache;
    private final ContactCache contactCache;
    private final AccessToken accessToken;

    /**
     * Construct a {@link UserDataStore} based on the database.
     * @param userCache A {@link UserCache} to retrieve the data.
     * @param accessToken A {@link AccessToken} that contains the current user id.
     * @param contactCache A {@link ContactCache} that contains the cached data of the user's possible contacts
     */
    public DiskUserDataStore(UserCache userCache, AccessToken accessToken, ContactCache contactCache) {
        this.userCache = userCache;
        this.accessToken = accessToken;
        this.contactCache = contactCache;
    }

    @Override
    public Observable<PinRealm> requestCode(String phoneNumber) {
        return null;
    }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(LoginEntity loginEntity) {
        return null;
    }

    @Override
    public Observable<AccessToken> register(String displayName, String username, LoginEntity loginEntity) { return null; }

    @Override
    public Observable<UserRealm> userInfos(String userId, String filterRecipient) {
        return this.userCache.userInfos(accessToken.getUserId());
    }

    @Override
    public Observable<Installation> createOrUpdateInstall(String token) {
        return null;
    }

    @Override
    public Observable<Installation> removeInstall() {
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
    public Observable<UserRealm> updateUser(List<Pair<String, String>> values) {
        return null;
    }

    @Override
    public Observable<List<ContactInterface>> contacts() {
        return contactCache.contacts().map(contactABRealms -> new ArrayList<>(contactABRealms));
    }

    @Override
    public Observable<List<ContactInterface>> contactsFB() {
        return contactCache.contactsFB().map(contactABRealms -> new ArrayList<>(contactABRealms));
    }

    @Override
    public Observable<Void> howManyFriends() {
        return null;
    }

    @Override
    public Observable<SearchResultRealm> findByUsername(String username) {
        return contactCache.findContactByUsername(username);
    }

    @Override
    public Observable<Boolean> lookupUsername(String username) {
        return null;
    }

    @Override
    public Observable<List<ContactABRealm>> findByValue(String username) {
        return contactCache.findContactsByValue(username);
    }

    @Override
    public Observable<FriendshipRealm> createFriendship(String userId) {
        return null;
    }

    @Override
    public Observable<Void> removeFriendship(String userId) {
        return null;
    }

    @Override
    public Observable<Void> notifyFBFriends() {
        return null;
    }

    @Override
    public Observable<GroupRealm> getGroupMembers(String groupId) {
        return null;
    }

    @Override
    public Observable<GroupRealm> getGroupInfos(String groupId) {
        return null;
    }

    @Override
    public Observable<MembershipRealm> createGroup(String groupName, List<String> memberIds, Boolean isPrivate, String pictureUri) {
        return null;
    }

    @Override
    public Observable<GroupRealm> updateGroup(String groupId, String groupName, String pictureUri) {
        return null;
    }

    @Override
    public Observable<Void> addMembersToGroup(String groupId, List<String> memberIds) {
        return null;
    }

    @Override
    public Observable<Void> removeMembersFromGroup(String groupId, List<String> memberIds) {
        return null;
    }

    @Override
    public Observable<Void> addAdminsToGroup(String groupId, List<String> memberIds) {
        return null;
    }

    @Override
    public Observable<Void> removeAdminsFromGroup(String groupId, List<String> memberIds) {
        return null;
    }

    @Override
    public Observable<Void> removeGroup(String groupId) {
        return null;
    }

    @Override
    public Observable<Void> leaveGroup(String groupId) {
        return null;
    }

    @Override
    public Observable<MembershipRealm> modifyPrivateGroupLink(String membershipId, boolean create) {
        return null;
    }

    @Override
    public Observable<Void> bootstrapSupport() {
        return null;
    }

    @Override
    public Observable<FriendshipRealm> updateFriendship(String friendshipId, @FriendshipRealm.FriendshipStatus String status) {
        return userCache.updateFriendship(friendshipId, status);
    }

    @Override
    public Observable<List<UserRealm>> updateUserListScore(Set<String> userIds) {
        return null;
    }

    @Override
    public Observable<String> getHeadDeepLink(String url) {
        return null;
    }

    @Override
    public Observable<MembershipRealm> createMembership(String groupId) {
        return null;
    }

    @Override
    public Observable<RecipientRealmInterface> getRecipientInfos(String recipientId, boolean isToGroup) {
        return Observable.just(isToGroup ? userCache.membershipForGroupId(recipientId) : userCache.friendshipForUserId(recipientId));
    }
}
