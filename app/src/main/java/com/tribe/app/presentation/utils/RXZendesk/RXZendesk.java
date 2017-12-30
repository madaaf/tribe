package com.tribe.app.presentation.utils.RXZendesk;

import android.net.Uri;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.preferences.SupportRequestId;
import com.tribe.app.presentation.utils.preferences.SupportUserId;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.widget.chat.model.Media;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageAudio;
import com.tribe.app.presentation.view.widget.chat.model.MessageImage;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
import com.zendesk.sdk.model.request.Comment;
import com.zendesk.sdk.model.request.CommentResponse;
import com.zendesk.sdk.model.request.CommentsResponse;
import com.zendesk.sdk.model.request.CreateRequest;
import com.zendesk.sdk.model.request.EndUserComment;
import com.zendesk.sdk.model.request.UploadResponse;
import com.zendesk.sdk.network.RequestProvider;
import com.zendesk.sdk.network.UploadProvider;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_AUDIO;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_IMAGE;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_TEXT;

/**
 * Created by madaaflak on 20/12/2017.
 */

@Singleton public class RXZendesk {
  private final String AUDIO_MEDIA_TYPE = "audio/mp4";
  private final String IMAGE_MEDIA_TYPE = "image/jpg";

  private final RequestProvider provider;
  private final UploadProvider uploadProvider;
  private final User user;
  private final DateUtils dateUtils;
  private Preference<String> supportUserIdPref;
  private Preference<String> supportIdPref;
  private Observable<List<Message>> messageListObservable;
  private Observable<Boolean> isMessageSend;
  private Observable<Boolean> isRequestZendeskCreated;
  private String supportId;

  @Inject public RXZendesk(User user, DateUtils dateUtils,
      @SupportUserId Preference<String> supportUserIdPref,
      @SupportRequestId Preference<String> supportIdPref) {
    this.user = user;
    this.dateUtils = dateUtils;
    this.provider = ZendeskConfig.INSTANCE.provider().requestProvider();
    this.uploadProvider = ZendeskConfig.INSTANCE.provider().uploadProvider();
    this.supportUserIdPref = supportUserIdPref;
    this.supportIdPref = supportIdPref;
    supportId = supportIdPref.get();
  }

  public Observable<List<Message>> getMessageZendesk() {
    messageListObservable = Observable.create((Subscriber<? super List<Message>> subscriber) -> {
      emitFriends(subscriber);
    }).onBackpressureBuffer().serialize();
    return messageListObservable;
  }

  public Observable<Boolean> addMessageZendesk(String typeMedia, String message, Uri uri) {
    isMessageSend = Observable.create((Subscriber<? super Boolean> subscriber) -> {
      isMessageSend(subscriber, typeMedia, message, uri);
    }).onBackpressureBuffer().serialize();
    return isMessageSend;
  }

  public Observable<Boolean> createRequestZendesk(String firstMessage) {
    isRequestZendeskCreated = Observable.create((Subscriber<? super Boolean> subscriber) -> {
      createRequestZendesk(subscriber, firstMessage);
    }).onBackpressureBuffer().serialize();
    return isRequestZendeskCreated;
  }

  public void createRequestZendesk(Subscriber subscriber, String firstMessage) {
    CreateRequest request = new CreateRequest();
    request.setSubject("Chat with " + user.getDisplayName());
    request.setDescription(firstMessage);
    request.setTags(Arrays.asList("chat", "mobile"));

    provider.createRequest(request, new ZendeskCallback<CreateRequest>() {
      @Override public void onSuccess(CreateRequest createRequest) {
        Timber.i("onSuccess create zendesk request : " + createRequest.getId());
        supportIdPref.set(createRequest.getId());
        supportId = createRequest.getId();
        getMessageZendesk();
        subscriber.onNext(true);
        subscriber.onCompleted();
      }

      @Override public void onError(ErrorResponse errorResponse) {
        Timber.e("onError create zendesk request" + errorResponse.getReason());
        subscriber.onNext(false);
        subscriber.onCompleted();
      }
    });
  }

  public void emitFriends(Subscriber subscriber) {
    List<Message> messages = new ArrayList<>();
    provider.getComments(supportId, new ZendeskCallback<CommentsResponse>() {
      @Override public void onSuccess(CommentsResponse commentsResponse) {
        Timber.i("onSuccess getCommentZendesk" + commentsResponse.getComments().size());
        String supportId = null;
        for (com.zendesk.sdk.model.request.User u : commentsResponse.getUsers()) {
          if (u.isAgent() && u.getId() != null) {
            supportId = u.getId().toString();
          } else if (!u.isAgent() && u.getId() != null) {
            supportUserIdPref.set(u.getId().toString());
          }
        }
        for (CommentResponse response : commentsResponse.getComments()) {
          // IMAGE MESSAGE
          if (!response.getAttachments().isEmpty()) {

            if (response.getAttachments().get(0).getContentType() != null
                && response.getAttachments().get(0).getContentType().equals(AUDIO_MEDIA_TYPE)) {
              MessageAudio audio = new MessageAudio();
              if (response.getId() != null) audio.setId(response.getId().toString());
              if (response.getAuthorId() != null && response.getAuthorId()
                  .toString()
                  .equals(supportId)) {
                audio.setAuthor(ShortcutUtil.createUserSupport());
              } else {
                audio.setAuthor(user);
              }
              audio.setCreationDate(dateUtils.dateToDateForMessage(response.getCreatedAt()));
              Media i = new Media();
              i.setUrl(response.getAttachments().get(0).getContentUrl());
              List<Media> list = new ArrayList<>();
              list.add(i);
              audio.setOriginal(i);
              audio.setRessources(list);
              audio.setType(MESSAGE_AUDIO);
              audio.setSupportAuthorId(response.getAuthorId().toString());
              messages.add(audio);
            } else {
              MessageImage image = new MessageImage();
              if (response.getId() != null) image.setId(response.getId().toString());
              if (response.getAuthorId() != null && response.getAuthorId()
                  .toString()
                  .equals(supportId)) {
                image.setAuthor(ShortcutUtil.createUserSupport());
              } else {
                image.setAuthor(user);
              }
              image.setCreationDate(dateUtils.dateToDateForMessage(response.getCreatedAt()));
              Media i = new Media();
              i.setUrl(response.getAttachments().get(0).getContentUrl());
              List<Media> list = new ArrayList<>();
              list.add(i);
              image.setOriginal(i);
              image.setRessources(list);
              image.setType(MESSAGE_IMAGE);
              image.setSupportAuthorId(response.getAuthorId().toString());
              messages.add(image);
            }
          } else {
            MessageText m = new MessageText();
            if (response.getId() != null) m.setId(response.getId().toString());
            if (response.getAuthorId() != null && response.getAuthorId()
                .toString()
                .equals(supportId)) {
              m.setAuthor(ShortcutUtil.createUserSupport());
            } else {
              m.setAuthor(user);
            }
            m.setCreationDate(dateUtils.dateToDateForMessage(response.getCreatedAt()));
            m.setMessage(response.getBody());
            m.setType(MESSAGE_TEXT);
            m.setSupportAuthorId(response.getAuthorId().toString());
            messages.add(m);
          }
        }

        subscriber.onNext(messages);
        subscriber.onCompleted();
      }

      @Override public void onError(ErrorResponse errorResponse) {
        Timber.e("onError getCommentZendesk " + errorResponse.getReason());
      }
    });
  }

  private boolean isAttachement(String typeMedia) {
    return (typeMedia.equals(MessageRealm.IMAGE) || typeMedia.equals(MessageRealm.AUDIO));
  }

  public void isMessageSend(Subscriber subscriber, String typeMedia, String data, Uri uri) {
    String name = "";
    String fileType = "";
    if (typeMedia.equals(MessageRealm.IMAGE)) {
      name = "image.jpg";
      fileType = IMAGE_MEDIA_TYPE;
    } else if (typeMedia.equals(MessageRealm.AUDIO)) {
      name = "note.mp4";
      fileType = AUDIO_MEDIA_TYPE;
    }
    if (isAttachement(typeMedia)) {
      File fileToUpload = new File(uri.getPath());
      uploadProvider.uploadAttachment(name, fileToUpload, fileType,
          new ZendeskCallback<UploadResponse>() {
            @Override public void onSuccess(UploadResponse uploadResponse) {
              Timber.i("success uploadAttachment to Zendesk" + uploadResponse.getAttachment());
              sendToZendesk(subscriber, "attachment : ", uploadResponse.getToken());
            }

            @Override public void onError(ErrorResponse errorResponse) {
              Timber.e("onError UploadResponse to Zendesk " + errorResponse.getReason());
            }
          });
    } else {
      sendToZendesk(subscriber, data, null);
    }
  }

  private void sendToZendesk(Subscriber subscriber, String data, String token) {
    List<String> attachmentList = new ArrayList<>();
    attachmentList.add(token);
    EndUserComment o = new EndUserComment();
    o.setValue(data);
    o.setAttachments(attachmentList);

    provider.addComment(supportId, o, new ZendeskCallback<Comment>() {
      @Override public void onSuccess(Comment comment) {
        Timber.i("onSuccess add comment to zendesk " + comment.getBody());
        subscriber.onNext(true);
        subscriber.onCompleted();
        // getMessageZendesk();
      }

      @Override public void onError(ErrorResponse errorResponse) {
        Timber.e("onError add comment to Zendesk " + errorResponse);
        subscriber.onNext(false);
        subscriber.onCompleted();
      }
    });
  }
}
