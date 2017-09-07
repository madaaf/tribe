package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.view.widget.chat.Message;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Singleton;

/**
 * Mapper class used to transform {@link FriendshipRealm} (in the data layer) to {@link Friendship}
 * in the
 * domain layer.
 */
@Singleton public class MessageRealmDataMapper {

  private UserRealmDataMapper userRealmDataMapper;

  public MessageRealmDataMapper(UserRealmDataMapper userRealmDataMapper) {
    this.userRealmDataMapper = userRealmDataMapper;
  }

  /**
   * Transform a {@link MessageRealm} into an {@link message}.
   *
   * @param messageRealm Object to be transformed.
   * @return {@link Message} if valid {@link MessageRealm} otherwise null.
   */
  public Message transform(MessageRealm messageRealm) {
    Message message = null;
    if (messageRealm != null) {
      message = new Message(messageRealm.getId());
      message.setAuthor(userRealmDataMapper.transform(messageRealm.getAuthor(), false));
    }

    return message;
  }

  /**
   * Transform a List of {@link FriendshipRealm} into a Collection of {@link Friendship}.
   *
   * @param messageRealmCollection Object Collection to be transformed.
   * @return {@link Friendship} if valid {@link FriendshipRealm} otherwise null.
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

  /**
   * Transform a {@link Friendship} into an {@link FriendshipRealm}.
   *
   * @param message Object to be transformed.
   * @return {@link FriendshipRealm} if valid {@link Friendship} otherwise null.
   */
  private MessageRealm transform(Message message) {
    MessageRealm messageRealm = null;
    if (message != null) {
      messageRealm = new MessageRealm(message.getId());
      messageRealm.setAuthor(userRealmDataMapper.transform(message.getAuthor(), false));
    }

    return messageRealm;
  }

  /**
   * Transform a List of {@link Friendship} into a Collection of {@link FriendshipRealm}.
   *
   * @param messageCollection Object Collection to be transformed.
   * @return {@link FriendshipRealm} if valid {@link Friendship} otherwise null.
   */
  public RealmList<MessageRealm> transformMessages(Collection<Message> messageCollection) {
    RealmList<MessageRealm> messageRealmRealmList = new RealmList<>();
    MessageRealm messageRealm;

    for (Message message : messageCollection) {
      messageRealm = transform(message);
      if (messageRealm != null) {
        messageRealmRealmList.add(messageRealm);
      }
    }

    return messageRealmRealmList;
  }
}
