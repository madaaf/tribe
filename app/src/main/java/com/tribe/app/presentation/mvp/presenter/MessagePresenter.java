package com.tribe.app.presentation.mvp.presenter;

import android.widget.ImageView;
import com.tribe.app.domain.interactor.chat.CreateMessage;
import com.tribe.app.domain.interactor.chat.CreatedMessages;
import com.tribe.app.domain.interactor.chat.UserMessageInfos;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.view.widget.chat.Message;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by madaaflak on 06/09/2017.
 */

public class MessagePresenter implements Presenter {
  // VIEW ATTACHED
  private ChatMVPView chatMVPView;

  protected UserMessageInfos userMessageInfos;
  protected CreatedMessages createdMessages;
  protected CreateMessage createMessage;

  @Inject public MessagePresenter(UserMessageInfos userMessageInfos, CreateMessage createMessage,
      CreatedMessages createdMessages) {
    this.userMessageInfos = userMessageInfos;
    this.createMessage = createMessage;
    this.createdMessages = createdMessages;
  }

  public void getCreatedMessages() {
    createdMessages.execute(new GetMessageSubscriber());
  }

  public void loadMessage(String[] userIds) {
    userMessageInfos.setUserIds(userIds);
    userMessageInfos.execute(new loadMessageSubscriber());
  }

  public void createMessage(String[] userIds, String data, String type, ImageView imageView) {
    createMessage.setParams(userIds, data, type);
    createMessage.execute(new CreateMessageSubscriber(imageView));
  }

  @Override public void onViewAttached(MVPView view) {
    chatMVPView = (ChatMVPView) view;
  }

  @Override public void onViewDetached() {
    userMessageInfos.unsubscribe();
    chatMVPView = null;
  }

  private class loadMessageSubscriber extends DefaultSubscriber<List<Message>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      if (chatMVPView != null) chatMVPView.errorLoadingMessage();
    }

    @Override public void onNext(List<Message> messages) {
      if (chatMVPView != null) chatMVPView.successLoadingMessage(messages);
    }
  }

  private class CreateMessageSubscriber extends DefaultSubscriber<Message> {
    private ImageView imageView;

    public CreateMessageSubscriber(ImageView imageView) {
      this.imageView = imageView;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      if (chatMVPView != null) chatMVPView.errorMessageCreation();
    }

    @Override public void onNext(Message message) {
      if (chatMVPView != null) chatMVPView.successMessageCreated(message, imageView);
    }
  }

  private class GetMessageSubscriber extends DefaultSubscriber<Message> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      if (chatMVPView != null) chatMVPView.errorGetSubscribeMessage();
    }

    @Override public void onNext(Message message) {
      if (chatMVPView != null) chatMVPView.successGetSubscribeMessage(message);
    }
  }
}
