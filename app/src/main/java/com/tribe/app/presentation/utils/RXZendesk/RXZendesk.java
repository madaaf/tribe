package com.tribe.app.presentation.utils.RXZendesk;

import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.widget.chat.model.Image;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageImage;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
import com.zendesk.sdk.model.request.CommentResponse;
import com.zendesk.sdk.model.request.CommentsResponse;
import com.zendesk.sdk.network.RequestProvider;
import com.zendesk.sdk.network.UploadProvider;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_IMAGE;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_TEXT;

/**
 * Created by madaaflak on 20/12/2017.
 */

@Singleton public class RXZendesk {

  private final RequestProvider provider;
  private final UploadProvider uploadProvider;
  private final User user;
  private final DateUtils dateUtils;
  private Observable<List<Message>> messageListObservable;

  @Inject public RXZendesk(User user, DateUtils dateUtils) {
    this.user = user;
    this.dateUtils = dateUtils;
    this.provider = ZendeskConfig.INSTANCE.provider().requestProvider();
    this.uploadProvider = ZendeskConfig.INSTANCE.provider().uploadProvider();
  }

  public Observable<List<Message>> getComments(String supportId) {
    if (messageListObservable == null) {
      messageListObservable = Observable.create((Subscriber<? super List<Message>> subscriber) -> {
        emitFriends(subscriber, supportId);
      }).onBackpressureBuffer().serialize();
    }

    return messageListObservable;
  }

  public void emitFriends(Subscriber subscriber, String supportId) {
    List<Message> messages = new ArrayList<Message>();
    provider.getComments(supportId, new ZendeskCallback<CommentsResponse>() {
      @Override public void onSuccess(CommentsResponse commentsResponse) {
        Timber.i("onSuccess getCommentZendesk" + commentsResponse.getComments().size());
        String supportUserId = null;
        for (com.zendesk.sdk.model.request.User u : commentsResponse.getUsers()) {
          if (u.isAgent() && u.getId() != null) {
            supportUserId = u.getId().toString();
          }
        }
        for (CommentResponse response : commentsResponse.getComments()) {
          if (!response.getAttachments().isEmpty()) {
            MessageImage image = new MessageImage();
            if (response.getId() != null) image.setId(response.getId().toString());
            if (response.getAuthorId() != null && response.getAuthorId()
                .toString()
                .equals(supportUserId)) {
              image.setAuthor(ShortcutUtil.createUserSupport());
            } else {
              image.setAuthor(user);
            }
            image.setCreationDate(dateUtils.getUTCDateForMessage());
            Image i = new Image();
            i.setUrl(response.getAttachments().get(0).getContentUrl());
            List<Image> list = new ArrayList<Image>();
            list.add(i);
            image.setRessources(list);
            image.setType(MESSAGE_IMAGE);
            messages.add(image);
          }
          MessageText m = new MessageText();
          if (response.getId() != null) m.setId(response.getId().toString());
          if (response.getAuthorId() != null && response.getAuthorId()
              .toString()
              .equals(supportUserId)) {
            m.setAuthor(ShortcutUtil.createUserSupport());
          } else {
            m.setAuthor(user);
          }
          m.setCreationDate(dateUtils.getUTCDateForMessage());
          m.setMessage(response.getBody());
          m.setType(MESSAGE_TEXT);
          messages.add(m);
        }

        subscriber.onNext(messages);
        subscriber.onCompleted();
      }

      @Override public void onError(ErrorResponse errorResponse) {
        Timber.e("onError getCommentZendesk " + errorResponse.getReason());
      }
    });
  }

/*  public Observable<List<MessageRealm>> getCommenssts(String supportId) {
    List<Message> messages = new ArrayList<Message>();
    List<Message> messagesRealm = new ArrayList<Message>();

    provider.getComments(supportId, new ZendeskCallback<CommentsResponse>() {
      @Override public void onSuccess(CommentsResponse commentsResponse) {
        Timber.i("onSuccess getCommentZendesk" + commentsResponse.getComments().size());
        String supportUserId = null;
        for (com.zendesk.sdk.model.request.User u : commentsResponse.getUsers()) {
          if (u.isAgent() && u.getId() != null) {
            supportUserId = u.getId().toString();
          }
        }
        for (CommentResponse response : commentsResponse.getComments()) {
          if (!response.getAttachments().isEmpty()) {
            MessageImage image = new MessageImage();
            if (response.getId() != null) image.setId(response.getId().toString());
            if (response.getAuthorId() != null && response.getAuthorId()
                .toString()
                .equals(supportUserId)) {
              image.setAuthor(ShortcutUtil.createUserSupport());
            } else {
              image.setAuthor(user);
            }
            image.setCreationDate(dateUtils.getUTCDateForMessage());
            Image i = new Image();
            i.setUrl(response.getAttachments().get(0).getContentUrl());
            List<Image> list = new ArrayList<Image>();
            list.add(i);
            image.setRessources(list);
            image.setType(MESSAGE_IMAGE);
            messages.add(image);
          }
          MessageText m = new MessageText();
          if (response.getId() != null) m.setId(response.getId().toString());
          if (response.getAuthorId() != null && response.getAuthorId()
              .toString()
              .equals(supportUserId)) {
            m.setAuthor(ShortcutUtil.createUserSupport());
          } else {
            m.setAuthor(user);
          }
          m.setCreationDate(dateUtils.getUTCDateForMessage());
          m.setMessage(response.getBody());
          m.setType(MESSAGE_TEXT);
          messages.add(m);
        }
      }

      @Override public void onError(ErrorResponse errorResponse) {
        Timber.e("onError getCommentZendesk " + errorResponse.getReason());
      }
    });




*//*    if (contactsObservable == null) {
      contactsObservable =
          Observable.create((Subscriber<? super List<ContactABRealm>> subscriber) -> {
            //emit(null, withPhones, sorter, filter, subscriber);
            emitFast(subscriber);
          }).onBackpressureBuffer().serialize();
    }

    return contactsObservable;*//*
  }*/
}
