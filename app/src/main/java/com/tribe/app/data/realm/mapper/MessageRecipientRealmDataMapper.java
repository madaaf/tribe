package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.MessageRecipientRealm;
import com.tribe.app.domain.entity.MessageRecipient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.RealmList;

/**
 * Mapper class used to transform {@link com.tribe.app.data.realm.MessageRecipientRealm} (in the data layer) to {@link com.tribe.app.domain.entity.MessageRecipient} in the
 * domain layer.
 */
@Singleton
public class MessageRecipientRealmDataMapper {

    @Inject
    public MessageRecipientRealmDataMapper() {}

    /**
     * Transform a {@link com.tribe.app.data.realm.MessageRecipientRealm} into an {@link com.tribe.app.domain.entity.MessageRecipient}.
     *
     * @param messageRecipientRealm Object to be transformed.
     * @return {@link com.tribe.app.domain.entity.MessageRecipient} if valid {@link com.tribe.app.data.realm.MessageRecipientRealm} otherwise null.
     */
    public MessageRecipient transform(MessageRecipientRealm messageRecipientRealm) {
        MessageRecipient messageRecipient = null;

        if (messageRecipientRealm != null) {
            messageRecipient = new MessageRecipient();
            messageRecipient.setId(messageRecipientRealm.getId());
            messageRecipient.setIsSeen(messageRecipientRealm.isSeen());
            messageRecipient.setTo(messageRecipientRealm.getTo());
        }

        return messageRecipient;
    }

    /**
     * Transform a {@link MessageRecipient} into an {@link MessageRecipientRealm}.
     *
     * @param messageRecipient Object to be transformed.
     * @return {@link MessageRecipientRealm} if valid {@link MessageRecipient} otherwise null.
     */
    public MessageRecipientRealm transform(MessageRecipient messageRecipient) {
        MessageRecipientRealm messageRecipientRealm = null;

        if (messageRecipient != null) {
            messageRecipientRealm = new MessageRecipientRealm();
            messageRecipientRealm.setIsSeen(messageRecipient.isSeen());
            messageRecipientRealm.setId(messageRecipient.getId());
            messageRecipientRealm.setTo(messageRecipient.getTo());
        }

        return messageRecipientRealm;
    }

    /**
     * Transform a List of {@link MessageRecipient} into a Collection of {@link MessageRecipientRealm}.
     *
     * @param messageRecipientList Object Collection to be transformed.
     * @return {@link List < MessageRecipienRealm >} if valid {@link List<MessageRecipient>} otherwise empty list.
     */
    public RealmList<MessageRecipientRealm> transform(Collection<MessageRecipient> messageRecipientList) {
        RealmList<MessageRecipientRealm> messageRecipientRealmList = new RealmList<>();
        MessageRecipientRealm messageRecipientRealm;

        if (messageRecipientList != null) {
            for (MessageRecipient messageRecipient : messageRecipientList) {
                messageRecipientRealm = transform(messageRecipient);

                if (messageRecipientRealm != null) {
                    messageRecipientRealmList.add(messageRecipientRealm);
                }
            }
        }

        return messageRecipientRealmList;
    }

    /**
     * Transform a List of {@link MessageRecipientRealm} into a Collection of {@link MessageRecipient}.
     *
     * @param messageRecipientRealmList Object Collection to be transformed.
     * @return {@link List < MessageRecipient >} if valid {@link List<MessageRecipientRealm>} otherwise empty list.
     */
    public List<MessageRecipient> transform(List<MessageRecipientRealm> messageRecipientRealmList) {
        List<MessageRecipient> messageRecipientList = new ArrayList<>();
        MessageRecipient messageRecipient;

        if (messageRecipientRealmList != null) {
            for (MessageRecipientRealm messageRecipientRealm : messageRecipientRealmList) {
                messageRecipient = transform(messageRecipientRealm);

                if (messageRecipient != null) {
                    messageRecipientList.add(messageRecipient);
                }
            }
        }

        return messageRecipientList;
    }
}
