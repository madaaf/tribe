package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;

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

    GroupRealmDataMapper groupRealmDataMapper;
    UserRealmDataMapper userRealmDataMapper;
    FriendshipRealmDataMapper friendshipRealmDataMapper;

    @Inject
    public ChatRealmDataMapper(GroupRealmDataMapper groupRealmDataMapper,
                               UserRealmDataMapper userRealmDataMapper) {
        this.groupRealmDataMapper = groupRealmDataMapper;
        this.userRealmDataMapper = userRealmDataMapper;
        this.friendshipRealmDataMapper = new FriendshipRealmDataMapper(userRealmDataMapper);
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
            chatMessage.setTo(chatRealm.isToGroup() ? groupRealmDataMapper.transform(chatRealm.getGroup()) : friendshipRealmDataMapper.transform(chatRealm.getFriendshipRealm()));
            chatMessage.setType(chatRealm.getType());
            chatMessage.setFrom(userRealmDataMapper.transform(chatRealm.getFrom()));
            chatMessage.setRecordedAt(chatRealm.getRecordedAt());
            chatMessage.setCreatedAt(chatRealm.getCreatedAt());
            chatMessage.setUpdatedAt(chatRealm.getUpdatedAt());
            chatMessage.setToGroup(chatRealm.isToGroup());
            chatMessage.setMessageStatus(chatRealm.getMessageStatus());
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
                chatRealm.setGroup(groupRealmDataMapper.transform((Group) chatMessage.getTo()));
            } else {
                chatRealm.setFriendshipRealm(friendshipRealmDataMapper.transform((Friendship) chatMessage.getTo()));
            }

            chatRealm.setType(chatMessage.getType());
            chatRealm.setFrom(userRealmDataMapper.transform(chatMessage.getFrom()));
            chatRealm.setRecordedAt(chatMessage.getRecordedAt());
            chatRealm.setCreatedAt(chatMessage.getCreatedAt());
            chatRealm.setUpdatedAt(chatMessage.getUpdatedAt());
            chatRealm.setToGroup(chatMessage.isToGroup());
            chatRealm.setMessageStatus(chatMessage.getMessageStatus());
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
}
