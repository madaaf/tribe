package com.tribe.app.data.repository.user.datasource;

import android.util.Pair;

import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.ContactCache;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ChatRealm;
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
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.NewGroupEntity;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;

/**
 * {@link UserDataStore} implementation based on connections to the database.
 */
public class DiskUserDataStore implements UserDataStore {

    private final UserCache userCache;
    private final ContactCache contactCache;
    private final TribeCache tribeCache;
    private final ChatCache chatCache;
    private final AccessToken accessToken;

    /**
     * Construct a {@link UserDataStore} based on the database.
     * @param userCache A {@link UserCache} to retrieve the data.
     * @param accessToken A {@link AccessToken} that contains the current user id.
     * @param contactCache A {@link ContactCache} that contains the cached data of the user's possible contacts
     */
    public DiskUserDataStore(UserCache userCache, AccessToken accessToken, ContactCache contactCache,
                             TribeCache tribeCache, ChatCache chatCache) {
        this.userCache = userCache;
        this.accessToken = accessToken;
        this.contactCache = contactCache;
        this.chatCache = chatCache;
        this.tribeCache = tribeCache;
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
        return contactCache.contactsFB().map(contactFBRealms -> new ArrayList<>(contactFBRealms));
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
    public Observable<MembershipRealm> getMembershipInfos(String membershipId) {
        return Observable.just(userCache.membershipInfos(membershipId));
    }

    @Override
    public Observable<MembershipRealm> createGroup(NewGroupEntity newGroupEntity) {
        return null;
    }

    @Override
    public Observable<GroupRealm> updateGroup(String groupId, List<Pair<String, String>> values) {
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

    @Override
    public Observable<Void> updateMessagesReceivedToNotSeen() {
        Map<String, List<android.support.v4.util.Pair<String, Object>>> chatUpdates = new HashMap<>();
        Map<String, List<android.support.v4.util.Pair<String, Object>>> tribeUpdates = new HashMap<>();

        List<TribeRealm> tribeRealmList = tribeCache.tribesReceivedNoObs(null);
        List<ChatRealm> chatRealmList = chatCache.messagesReceivedNoObs();

        for (TribeRealm tribeRealm : tribeRealmList) {
            List<android.support.v4.util.Pair<String, Object>> values = new ArrayList<>();
            values.add(android.support.v4.util.Pair.create(TribeRealm.MESSAGE_RECEIVING_STATUS, MessageReceivingStatus.STATUS_NOT_SEEN));

            if (tribeRealm.isToGroup() && tribeRealm.getMembershipRealm() != null
                    && (tribeRealm.getMembershipRealm().getUpdatedAt() == null || tribeRealm.getMembershipRealm().getUpdatedAt().before(tribeRealm.getRecordedAt()))) {
                values.add(android.support.v4.util.Pair.create(TribeRealm.GROUP_ID_UPDATED_AT, tribeRealm.getRecordedAt()));
            } else if (!tribeRealm.isToGroup() && tribeRealm.getFrom() != null
                    && (tribeRealm.getFrom().getUpdatedAt() == null || tribeRealm.getFrom().getUpdatedAt().before(tribeRealm.getRecordedAt()))) {
                values.add(android.support.v4.util.Pair.create(TribeRealm.FRIEND_ID_UPDATED_AT, tribeRealm.getRecordedAt()));
            }

            tribeUpdates.put(tribeRealm.getLocalId(), values);
        }

        tribeCache.update(tribeUpdates);

        for (ChatRealm chatRealm : chatRealmList) {
            List<android.support.v4.util.Pair<String, Object>> values = new ArrayList<>();
            values.add(android.support.v4.util.Pair.create(ChatRealm.MESSAGE_RECEIVING_STATUS, MessageReceivingStatus.STATUS_NOT_SEEN));

            if (chatRealm.isToGroup() && chatRealm.getMembershipRealm() != null
                    && (chatRealm.getMembershipRealm().getUpdatedAt() == null || chatRealm.getMembershipRealm().getUpdatedAt().before(chatRealm.getRecordedAt()))) {
                values.add(android.support.v4.util.Pair.create(ChatRealm.GROUP_ID_UPDATED_AT, chatRealm.getRecordedAt()));
            } else if (!chatRealm.isToGroup() && chatRealm.getFrom() != null
                    && (chatRealm.getFrom().getUpdatedAt() == null || chatRealm.getFrom().getUpdatedAt().before(chatRealm.getRecordedAt()))) {
                values.add(android.support.v4.util.Pair.create(ChatRealm.FRIEND_ID_UPDATED_AT, chatRealm.getRecordedAt()));
            }

            chatUpdates.put(chatRealm.getLocalId(), values);
        }

        chatCache.update(chatUpdates);

        return Observable.just(null);
    }
}
