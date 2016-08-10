package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton
public class MessageRealmDataMapper {

    GroupRealmDataMapper groupRealmDataMapper;
    UserRealmDataMapper userRealmDataMapper;
    FriendshipRealmDataMapper friendshipRealmDataMapper;

    @Inject
    public MessageRealmDataMapper(GroupRealmDataMapper groupRealmDataMapper,
                                  UserRealmDataMapper userRealmDataMapper) {
        this.groupRealmDataMapper = groupRealmDataMapper;
        this.userRealmDataMapper = userRealmDataMapper;
        this.friendshipRealmDataMapper = new FriendshipRealmDataMapper(userRealmDataMapper);
    }

    /**
     * Transform a {@link MessageRealm} into an {@link Message}.
     *
     * @param messageRealm Object to be transformed.
     * @return {@link Message} if valid {@link MessageRealm} otherwise null.
     */
    public Message transform(MessageRealm messageRealm) {
        Message message = null;

        if (messageRealm != null) {
            message = new Message();
            message.setId(messageRealm.getId());
            message.setLocalId(messageRealm.getLocalId());
            message.setText(messageRealm.getText());
            message.setTo(messageRealm.isToGroup() ? groupRealmDataMapper.transform(messageRealm.getGroup()) : friendshipRealmDataMapper.transform(messageRealm.getFriendshipRealm()));
            message.setType(messageRealm.getType());
            message.setFrom(userRealmDataMapper.transform(messageRealm.getFrom()));
            message.setRecordedAt(messageRealm.getRecordedAt());
            message.setUpdatedAt(messageRealm.getUpdatedAt());
            message.setToGroup(messageRealm.isToGroup());
            message.setUrl(messageRealm.getUrl());
            message.setMessageStatus(messageRealm.getMessageStatus());
        }

        return message;
    }

    /**
     * Transform a {@link Message} into an {@link MessageRealm}.
     *
     * @param message Object to be transformed.
     * @return {@link MessageRealm} if valid {@link Message} otherwise null.
     */
    public MessageRealm transform(Message message) {
        MessageRealm messageRealm = null;

        if (messageRealm != null) {
            messageRealm = new MessageRealm();
            messageRealm.setId(message.getId());
            messageRealm.setLocalId(message.getLocalId());
            messageRealm.setText(message.getText());

            if (message.isToGroup()) {
                messageRealm.setGroup(groupRealmDataMapper.transform((Group) message.getTo()));
            } else {
                messageRealm.setFriendshipRealm(friendshipRealmDataMapper.transform((Friendship) message.getTo()));
            }

            messageRealm.setType(message.getType());
            messageRealm.setFrom(userRealmDataMapper.transform(message.getFrom()));
            messageRealm.setRecordedAt(message.getRecordedAt());
            messageRealm.setUpdatedAt(message.getUpdatedAt());
            messageRealm.setToGroup(message.isToGroup());
            messageRealm.setUrl(message.getUrl());
            messageRealm.setMessageStatus(message.getMessageStatus());
        }

        return messageRealm;
    }

    /**
     * Transform a List of {@link MessageRealm} into a Collection of {@link Message}.
     *
     * @param messageRealmCollection Object Collection to be transformed.
     * @return {@link Message} if valid {@link MessageRealm} otherwise null.
     */
    public List<Message> transform(Collection<MessageRealm> messageRealmCollection) {
        List<Message> messageList = new ArrayList<>();
        Message message;
        for (MessageRealm messageRealm : messageRealmCollection) {
            message = transform(messageRealm);
            if (message != null) {
                messageList.add(message);
            }
        }

        return messageList;
    }
}
