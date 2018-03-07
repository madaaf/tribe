package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.MediaRealm;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.presentation.view.widget.chat.model.Media;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageAudio;
import com.tribe.app.presentation.view.widget.chat.model.MessageEmoji;
import com.tribe.app.presentation.view.widget.chat.model.MessageEvent;
import com.tribe.app.presentation.view.widget.chat.model.MessageImage;
import com.tribe.app.presentation.view.widget.chat.model.MessagePoke;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
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
          message.setAuthor(userRealmDataMapper.transform(messageRealm.getAuthor(), true));
          break;
        case Message.MESSAGE_EMOJI:
          message = new MessageEmoji(messageRealm.getId());
          ((MessageEmoji) message).setEmoji(messageRealm.getData());
          message.setAuthor(userRealmDataMapper.transform(messageRealm.getAuthor(), true));
          break;
        case Message.MESSAGE_IMAGE:
          message = new MessageImage(messageRealm.getId());
          // MediaRealm o = messageRealm.getOriginal();
          List<MediaRealm> ressources = messageRealm.getAlts();
          if (!ressources.isEmpty()) {
            Media o = userRealmDataMapper.transformOriginalRealmList(ressources, false);
            ((MessageImage) message).setOriginal(o);
          } else {
            MediaRealm o = messageRealm.getOriginal();
            ((MessageImage) message).setOriginal(userRealmDataMapper.transform(o));
          }
            /*((MessageImage) message).setRessources(
              userRealmDataMapper.transformOriginalRealmList(ressources));*/
          message.setAuthor(userRealmDataMapper.transform(messageRealm.getAuthor(), true));
          break;
        case Message.MESSAGE_EVENT:
          message = new MessageEvent(messageRealm.getId());
          ((MessageEvent) message).setAction(messageRealm.getAction());
          ((MessageEvent) message).setUser(
              userRealmDataMapper.transform(messageRealm.getUser(), true));
          break;
        case Message.MESSAGE_AUDIO:
          message = new MessageAudio(messageRealm.getId());
          message.setAuthor(
              userRealmDataMapper.transform(messageRealm.getAuthor(), false)); //   TODO TIAGO
          List<MediaRealm> r = messageRealm.getAlts();
          if (!r.isEmpty()) {
            Media i = userRealmDataMapper.transformOriginalRealmList(r, true);
            ((MessageAudio) message).setOriginal(i);
          } else {
            Media i = userRealmDataMapper.transform(messageRealm.getOriginal());
            ((MessageAudio) message).setOriginal(i);
          }
          break;
        case Message.MESSAGE_POKE:
          message = new MessagePoke(messageRealm.getId());
          ((MessagePoke) message).setClientMessageId(messageRealm.getClient_message_id());
          ((MessagePoke) message).setIntent(messageRealm.getIntent());
          ((MessagePoke) message).setGameId(messageRealm.getGame_id());
          ((MessagePoke) message).setData(messageRealm.getData());
          break;
      }
      if (message != null) {
        message.setType(messageRealm.get__typename());
        message.setSupportAuthorId(messageRealm.getSupportAuthorId());
        message.setCreationDate(messageRealm.getCreated_at());
      }
    }

    return message;
  }

  public List<Message> transform(Collection<MessageRealm> messageRealmCollection) {
    List<Message> messageList = new ArrayList<>();
    Message message = null;

    for (MessageRealm messageRealm : messageRealmCollection) {
      if (messageRealm != null) {
        if (messageRealm.get__typename() != null) {
          message = transform(messageRealm);
        }
        if (message != null) {
          messageList.add(message);
        }
      }
    }

    return messageList;
  }

  public MessageRealm transform(Message message) {
    MessageRealm messageRealm = null;
    if (message != null) {
      messageRealm = new MessageRealm(message.getId());
      messageRealm.setAuthor(userRealmDataMapper.transform(message.getAuthor(), true));
      messageRealm.set__typename(message.getType());
      messageRealm.setSupportAuthorId(message.getSupportAuthorId());
      messageRealm.setCreated_at(message.getCreationDate());

      switch (message.getType()) {
        case Message.MESSAGE_TEXT:
          messageRealm.setData(((MessageText) message).getMessage());
          break;
        case Message.MESSAGE_EMOJI:
          messageRealm.setData(((MessageEmoji) message).getEmoji());
          break;
        case Message.MESSAGE_IMAGE:
          Media o = ((MessageImage) message).getOriginal();
          messageRealm.setOriginal(userRealmDataMapper.transform(o));
          //List<Media> ressources = ((MessageImage) message).getRessources();
          //messageRealm.setAlts(userRealmDataMapper.transformOriginalList(ressources));
          break;
        case Message.MESSAGE_EVENT:
          messageRealm.setAction(((MessageEvent) message).getAction());
          messageRealm.setUser(
              userRealmDataMapper.transform(((MessageEvent) message).getUser(), true));
          break;

        case Message.MESSAGE_AUDIO:
          Media o2 = ((MessageAudio) message).getOriginal();
          messageRealm.setOriginal(userRealmDataMapper.transform(o2));
          break;

        case Message.MESSAGE_POKE:
          messageRealm.setClient_message_id(((MessagePoke) message).getClientMessageId());
          messageRealm.setIntent(((MessagePoke) message).getIntent());
          messageRealm.setGame_id(((MessagePoke) message).getGameId());
          messageRealm.setData(((MessagePoke) message).getData());

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
