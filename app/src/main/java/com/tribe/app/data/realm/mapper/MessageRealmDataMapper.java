package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.MessageRealm;
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

    @Inject
    public MessageRealmDataMapper() {

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
            message = new Message(messageRealm.getId());
            message.setCreatedAt(messageRealm.getCreatedAt());
            message.setUpdatedAt(messageRealm.getUpdatedAt());
            message.setReceiverId(messageRealm.getReceiverId());
            message.setSenderId(messageRealm.getSenderId());
            message.setText(messageRealm.getText());
        }

        return message;
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
