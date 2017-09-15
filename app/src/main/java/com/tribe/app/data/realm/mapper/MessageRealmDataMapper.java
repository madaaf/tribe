package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.ImageRealm;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.presentation.view.widget.chat.Image;
import com.tribe.app.presentation.view.widget.chat.Message;
import com.tribe.app.presentation.view.widget.chat.MessageEmoji;
import com.tribe.app.presentation.view.widget.chat.MessageEvent;
import com.tribe.app.presentation.view.widget.chat.MessageImage;
import com.tribe.app.presentation.view.widget.chat.MessageText;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class MessageRealmDataMapper {

  private UserRealmDataMapper userRealmDataMapper;

  @Inject public MessageRealmDataMapper(UserRealmDataMapper userRealmDataMapper) {
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
        case Message.MESSAGE_TEXT:
          message = new MessageText(messageRealm.getId());
          ((MessageText) message).setMessage(messageRealm.getData());
          break;
        case Message.MESSAGE_EMOJI:
          message = new MessageEmoji(messageRealm.getId());
          ((MessageEmoji) message).setEmoji(messageRealm.getData());
          break;
        case Message.MESSAGE_IMAGE:
          message = new MessageImage(messageRealm.getId());
          ImageRealm o = messageRealm.getOriginal();
          List<ImageRealm> ressources = messageRealm.getAlts();
          ((MessageImage) message).setOriginal(userRealmDataMapper.transform(o));
          ((MessageImage) message).setRessources(
              userRealmDataMapper.transformOriginalRealmList(ressources));
          break;
        case Message.MESSAGE_EVENT:
          message = new MessageEvent(messageRealm.getId());
          ((MessageEvent) message).setAction(messageRealm.getAction());
          ((MessageEvent) message).setUser(userRealmDataMapper.transform(messageRealm.getUser()));
          break;
      }

      message.setAuthor(userRealmDataMapper.transform(messageRealm.getAuthor()));
      message.setType(messageRealm.get__typename());
    }

    return message;
  }

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

  private MessageRealm transform(Message message) {
    MessageRealm messageRealm = null;
    if (message != null) {
      messageRealm = new MessageRealm(message.getId());
      messageRealm.setAuthor(userRealmDataMapper.transform(message.getAuthor()));
      messageRealm.set__typename(message.getType());

      switch (message.getType()) {
        case Message.MESSAGE_TEXT:
          messageRealm.setData(((MessageText) message).getMessage());
          break;
        case Message.MESSAGE_EMOJI:
          messageRealm.setData(((MessageEmoji) message).getEmoji());
          break;
        case Message.MESSAGE_IMAGE:
          Image o = ((MessageImage) message).getOriginal();
          List<Image> ressources = ((MessageImage) message).getRessources();
          messageRealm.setOriginal(userRealmDataMapper.transform(o));
          messageRealm.setAlts(userRealmDataMapper.transformOriginalList(ressources));
          break;
        case Message.MESSAGE_EVENT:
          messageRealm.setAction(((MessageEvent) message).getAction());
          messageRealm.setUser(userRealmDataMapper.transform(((MessageEvent) message).getUser()));
          break;
      }
    }

    return messageRealm;
  }

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
