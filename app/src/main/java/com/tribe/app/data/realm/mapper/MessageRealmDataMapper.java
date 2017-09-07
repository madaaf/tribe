package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.view.widget.chat.Message;
import com.tribe.app.presentation.view.widget.chat.MessageEmoji;
import com.tribe.app.presentation.view.widget.chat.MessageImage;
import com.tribe.app.presentation.view.widget.chat.MessageText;
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
   * Transform a {@link MessageRealm} into an {@link }.
   *
   * @param messageRealm Object to be transformed.
   * @return {@link Message} if valid {@link MessageRealm} otherwise null.
   */
  public Message transform(MessageRealm messageRealm) {
    Message message = null;
    if (messageRealm != null) {
      switch (messageRealm.get__typename()) {
        case MessageRealm.MESSAGE_TEXT:
          message = new MessageText(messageRealm.getId());
          ((MessageText) message).setMessage(messageRealm.getData());
          break;
        case MessageRealm.EMOJI:
          message = new MessageEmoji(messageRealm.getId());
          ((MessageEmoji) message).setEmoji(messageRealm.getData());
          break;
        case MessageRealm.IMAGE:
          message = new MessageImage(messageRealm.getId());
          break;
      }

      message.setAuthor(userRealmDataMapper.transform(messageRealm.getAuthor(), false));
      message.setType(messageRealm.get__typename());
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
      messageRealm.set__typename(message.getType());

      switch (message.getType()) {
        case MessageRealm.MESSAGE_TEXT:
          messageRealm.setData(((MessageText) message).getMessage());
          break;
        case MessageRealm.EMOJI:
          messageRealm.setData(((MessageEmoji) message).getEmoji());
          break;
        case MessageRealm.IMAGE:
          break;
      }
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
