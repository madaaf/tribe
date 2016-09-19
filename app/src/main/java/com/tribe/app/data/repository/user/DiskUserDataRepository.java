package com.tribe.app.data.repository.user;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.mapper.ChatRealmDataMapper;
import com.tribe.app.data.realm.mapper.ContactRealmDataMapper;
import com.tribe.app.data.realm.mapper.SearchResultRealmDataMapper;
import com.tribe.app.data.realm.mapper.TribeRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.chat.datasource.ChatDataStore;
import com.tribe.app.data.repository.chat.datasource.ChatDataStoreFactory;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStore;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStoreFactory;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStoreFactory;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.SearchResult;
import com.tribe.app.domain.entity.TribeMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.user.UserRepository;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.component.PullToSearchView;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * {@link DiskUserDataRepository} for retrieving user data.
 */
@Singleton
public class DiskUserDataRepository implements UserRepository {

    private final UserDataStoreFactory userDataStoreFactory;
    private final UserRealmDataMapper userRealmDataMapper;
    private final TribeDataStoreFactory tribeDataStoreFactory;
    private final TribeRealmDataMapper tribeRealmDataMapper;
    private final ChatDataStoreFactory chatDataStoreFactory;
    private final ChatRealmDataMapper chatRealmDataMapper;
    private final ContactRealmDataMapper contactRealmDataMapper;
    private final SearchResultRealmDataMapper searchResultRealmDataMapper;

    /**
     * Constructs a {@link UserRepository}.
     *
     * @param dataStoreFactory A factory to construct different data source implementations.
     * @param realmDataMapper {@link UserRealmDataMapper}.
     */
    @Inject
    public DiskUserDataRepository(UserDataStoreFactory dataStoreFactory,
                                  UserRealmDataMapper realmDataMapper,
                                  TribeDataStoreFactory tribeDataStoreFactory,
                                  TribeRealmDataMapper tribeRealmDataMapper,
                                  ChatDataStoreFactory chatDataStoreFactory,
                                  ChatRealmDataMapper chatRealmDataMapper,
                                  ContactRealmDataMapper contactRealmDataMapper) {
        this.userDataStoreFactory = dataStoreFactory;
        this.userRealmDataMapper = realmDataMapper;
        this.tribeDataStoreFactory = tribeDataStoreFactory;
        this.tribeRealmDataMapper = tribeRealmDataMapper;
        this.chatDataStoreFactory = chatDataStoreFactory;
        this.chatRealmDataMapper = chatRealmDataMapper;
        this.contactRealmDataMapper = contactRealmDataMapper;
        this.searchResultRealmDataMapper = new SearchResultRealmDataMapper(userRealmDataMapper.getFriendshipRealmDataMapper());
    }

    @Override
    public Observable<Pin> requestCode(String phoneNumber) { return null; }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(LoginEntity loginEntity) { return null; }

    @Override
    public Observable<AccessToken> register(String displayName, String username, LoginEntity loginEntity) { return null; }

    @Override
    public Observable<User> userInfos(String userId, String filterRecipient) {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createDiskDataStore();
        final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
        final ChatDataStore chatDataStore = this.chatDataStoreFactory.createDiskChatStore();

        return Observable.combineLatest(
                tribeDataStore.tribesNotSeen(null).map(collection -> tribeRealmDataMapper.transform(collection)),
                userDataStore
                        .userInfos(null, filterRecipient)
                        .map(userRealm -> userRealmDataMapper.transform(userRealm))
                        .map(user -> {
                            if (!StringUtils.isEmpty(filterRecipient) && !filterRecipient.equals(PullToSearchView.HOME)) {
                                List<Friendship> filteredFriendshipList = new ArrayList<>();
                                List<Group> filteredGroupList = new ArrayList<>();

                                for (Friendship friendship : user.getFriendships()) {
                                    if (PullToSearchView.shouldFilter(filterRecipient, friendship)) {
                                        filteredFriendshipList.add(friendship);
                                    }
                                }

                                user.setFriendships(filteredFriendshipList);

                                for (Group group : user.getGroupList()) {
                                    if (PullToSearchView.shouldFilter(filterRecipient, group)) {
                                        filteredGroupList.add(group);
                                    }
                                }

                                user.setGroupList(filteredGroupList);
                            }

                            return user;
                        }),
                chatDataStore
                        .messages(null)
                        .map(collection -> chatRealmDataMapper.transform(collection)),
                (tribes, user, chatMessages) -> {
                    List<Recipient> result = user.getFriendshipList();

                    for (Recipient recipient : result) {
                        List<TribeMessage> receivedTribes = new ArrayList<>();
                        List<TribeMessage> sentTribes = new ArrayList<>();
                        List<TribeMessage> errorTribes = new ArrayList<>();
                        List<ChatMessage> receivedChatMessage = new ArrayList<>();

                        for (TribeMessage tribe : tribes) {
                            if (tribe.getFrom() != null) {
                                if (!tribe.getFrom().getId().equals(user.getId()) && (tribe.isToGroup() && tribe.getTo().getId().equals(recipient.getId()))
                                        || (!tribe.isToGroup() && tribe.getFrom().getId().equals(recipient.getId()))) {
                                    receivedTribes.add(tribe);
                                } else if (tribe.getFrom().getId().equals(user.getId())
                                        && tribe.getTo().getId().equals(recipient.getId())) {
                                    if (tribe.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_ERROR))
                                        errorTribes.add(tribe);
                                    else sentTribes.add(tribe);
                                }
                            }
                        }

                        recipient.setErrorTribes(errorTribes);
                        recipient.setReceivedTribes(receivedTribes);
                        recipient.setSentTribes(sentTribes);

                        for (ChatMessage chatMessage : chatMessages) {
                            if (chatMessage.getFrom() != null) {
                                if (!chatMessage.getFrom().getId().equals(user.getId()) && (chatMessage.isToGroup() && chatMessage.getTo().getId().equals(recipient.getId()))
                                        || (!chatMessage.isToGroup() && chatMessage.getFrom().getId().equals(recipient.getId()))) {
                                    receivedChatMessage.add(chatMessage);
                                }
                            }
                        }

                        recipient.setReceivedMessages(receivedChatMessage);
                    }

                    Friendship friendship = new Friendship(user.getId());
                    friendship.setFriend(user);
                    result.add(0, friendship);

                    return user;
                }
        );
    }

    @Override
    public Observable<User> updateUser(String username, String displayName, String pictureUri, String fbid) {
        return null;
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
    public Observable<List<Message>> messages() {
        return null;
    }

    @Override
    public Observable<List<Message>> messagesReceived(String friendshipId) {
        final TribeDataStore tribeDataStore = this.tribeDataStoreFactory.createDiskDataStore();
        final ChatDataStore chatDataStore = this.chatDataStoreFactory.createDiskChatStore();

        return Observable.combineLatest(tribeDataStore.tribesReceived(null).map(collection -> tribeRealmDataMapper.transform(collection)),
                chatDataStore.messagesReceived(null).map(collection -> chatRealmDataMapper.transform(collection)),
                (tribes, chatMessages) -> {
                    List<Message> messageList = new ArrayList<>();
                    messageList.addAll(tribes);
                    messageList.addAll(chatMessages);
                    return messageList;
                }
        );
    }

    @Override
    public Observable<List<Contact>> contacts() {
        final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
        return userDataStore.contacts().map(collection -> contactRealmDataMapper.transform(new ArrayList<ContactInterface>(collection)));
    }

    @Override
    public Observable<Void> howManyFriends() {
        return null;
    }

    @Override
    public Observable<SearchResult> findByUsername(String usernamve) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
        return userDataStore.findByUsername(usernamve).map(searchResultRealm -> searchResultRealmDataMapper.transform(searchResultRealm));
    }

    @Override
    public Observable<Boolean> lookupUsername(String username) {
        return null;
    }

    @Override
    public Observable<List<Contact>> findByValue(String value) {
        final UserDataStore userDataStore = this.userDataStoreFactory.createDiskDataStore();
        return userDataStore.findByValue(value).map(collection -> contactRealmDataMapper.transform(new ArrayList<ContactInterface>(collection)));
    }

    // TODO: update info in DB
    @Override
    public Observable<Friendship> createFriendship(String userId) {
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
    public Observable<Group> getGroupMembers(String groupId) {
        return null;
    }

    @Override
    public Observable<Group> createGroup(String groupName, List<String> memberIds, boolean isPrivate, String pictureUri) {
        return null;
    }

    @Override
    public Observable<Group> updateGroup(String groupId, String groupName, String pictureUri) {
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
}
