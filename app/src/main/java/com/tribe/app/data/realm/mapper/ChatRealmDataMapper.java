package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton
public class ChatRealmDataMapper {

    private final MembershipRealmDataMapper membershipRealmDataMapper;
    private final UserRealmDataMapper userRealmDataMapper;
    private final FriendshipRealmDataMapper friendshipRealmDataMapper;
    private final MessageRecipientRealmDataMapper messageRecipientRealmDataMapper;

    @Inject
    public ChatRealmDataMapper(GroupRealmDataMapper groupRealmDataMapper,
                               UserRealmDataMapper userRealmDataMapper,
                               MessageRecipientRealmDataMapper messageRecipientRealmDataMapper) {
        this.membershipRealmDataMapper = new MembershipRealmDataMapper(groupRealmDataMapper);
        this.userRealmDataMapper = userRealmDataMapper;
        this.friendshipRealmDataMapper = new FriendshipRealmDataMapper(userRealmDataMapper);
        this.messageRecipientRealmDataMapper = messageRecipientRealmDataMapper;
    }

    /**
     * Transform a {@link ChatRealm} into an {@link ChatMessage}.
     *
     * @param chatRealm Object to be transformed.
     * @return {@link ChatMessage} if valid {@link ChatRealm} otherwise null.
     */
    public ChatMessage transform(ChatRealm chatRealm) {
        ChatMessage chatMessage = null;

        if (chatRealm != null) {
            chatMessage = new ChatMessage();
            chatMessage.setId(chatRealm.getId());
            chatMessage.setLocalId(chatRealm.getLocalId());
            chatMessage.setContent(chatRealm.getContent());
            chatMessage.setTo(chatRealm.isToGroup() ? membershipRealmDataMapper.transform(chatRealm.getMembershipRealm()) : friendshipRealmDataMapper.transform(chatRealm.getFriendshipRealm()));
            chatMessage.setType(chatRealm.getType());
            chatMessage.setFrom(userRealmDataMapper.transform(chatRealm.getFrom(), true));
            chatMessage.setRecordedAt(chatRealm.getRecordedAt());
            chatMessage.setCreatedAt(chatRealm.getCreatedAt());
            chatMessage.setUpdatedAt(chatRealm.getUpdatedAt());
            chatMessage.setToGroup(chatRealm.isToGroup());
            chatMessage.setMessageSendingStatus(chatRealm.getMessageSendingStatus());
            chatMessage.setMessageDownloadingStatus(chatRealm.getMessageDownloadingStatus());
            chatMessage.setMessageReceivingStatus(chatRealm.getMessageReceivingStatus());
            chatMessage.setRecipientList(messageRecipientRealmDataMapper.transform(chatRealm.getRecipientList()));
            chatMessage.setProgress(chatRealm.getProgress());
            chatMessage.setTotalSize(chatRealm.getTotalSize());
        }

        return chatMessage;
    }

    /**
     * Transform a {@link ChatMessage} into an {@link ChatRealm}.
     *
     * @param chatMessage Object to be transformed.
     * @return {@link ChatRealm} if valid {@link ChatMessage} otherwise null.
     */
    public ChatRealm transform(ChatMessage chatMessage) {
        ChatRealm chatRealm = null;

        if (chatMessage != null) {
            chatRealm = new ChatRealm();
            chatRealm.setId(chatMessage.getId());
            chatRealm.setLocalId(chatMessage.getLocalId());
            chatRealm.setContent(chatMessage.getContent());

            if (chatMessage.isToGroup()) {
                chatRealm.setMembershipRealm(membershipRealmDataMapper.transform((Membership) chatMessage.getTo()));
            } else {
                chatRealm.setFriendshipRealm(friendshipRealmDataMapper.transform((Friendship) chatMessage.getTo()));
            }

            chatRealm.setType(chatMessage.getType());
            chatRealm.setFrom(userRealmDataMapper.transform(chatMessage.getFrom(), true));
            chatRealm.setRecordedAt(chatMessage.getRecordedAt());
            chatRealm.setCreatedAt(chatMessage.getCreatedAt());
            chatRealm.setUpdatedAt(chatMessage.getUpdatedAt());
            chatRealm.setToGroup(chatMessage.isToGroup());
            chatRealm.setMessageSendingStatus(chatMessage.getMessageSendingStatus());
            chatRealm.setMessageDownloadingStatus(chatMessage.getMessageDownloadingStatus());
            chatRealm.setMessageReceivingStatus(chatMessage.getMessageReceivingStatus());
            chatRealm.setRecipientList(messageRecipientRealmDataMapper.transform(chatMessage.getRecipientList()));
            chatRealm.setProgress(chatMessage.getProgress());
            chatRealm.setTotalSize(chatMessage.getTotalSize());
        }

        return chatRealm;
    }

    /**
     * Transform a List of {@link ChatRealm} into a Collection of {@link ChatMessage}.
     *
     * @param chatRealmCollection Object Collection to be transformed.
     * @return {@link ChatMessage} if valid {@link ChatRealm} otherwise null.
     */
    public List<ChatMessage> transform(Collection<ChatRealm> chatRealmCollection) {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        ChatMessage chatMessage;
        for (ChatRealm chatRealm : chatRealmCollection) {
            chatMessage = transform(chatRealm);
            if (chatMessage != null) {
                chatMessageList.add(chatMessage);
            }
        }

        return chatMessageList;
    }

    /**
     * Transform a List of {@link ChatMessage} into a Collection of {@link ChatRealm}.
     *
     * @param chatMessageCollection Object Collection to be transformed.
     * @return {@link List<ChatRealm>} if valid {@link ChatMessage} otherwise null.
     */
    public List<ChatRealm> transform(List<ChatMessage> chatMessageCollection) {
        List<ChatRealm> chatRealmList = new ArrayList<>();
        ChatRealm chatRealm;
        for (ChatMessage chatMessage : chatMessageCollection) {
            chatRealm = transform(chatMessage);
            if (chatMessage != null) {
                chatRealmList.add(chatRealm);
            }
        }

        return chatRealmList;
    }
}
