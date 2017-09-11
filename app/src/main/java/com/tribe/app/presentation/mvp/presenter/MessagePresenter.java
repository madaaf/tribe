package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.CreateMessage;
import com.tribe.app.domain.interactor.user.UserMessageInfos;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.view.widget.chat.Message;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by madaaflak on 06/09/2017.
 */

public class MessagePresenter implements Presenter {
  // VIEW ATTACHED
  private ChatMVPView chatMVPView;

  protected UserMessageInfos userMessageInfos;
  protected CreateMessage createMessage;

  @Inject public MessagePresenter(UserMessageInfos userMessageInfos, CreateMessage createMessage) {
    this.userMessageInfos = userMessageInfos;
    this.createMessage = createMessage;
  }

  public void loadMessage() { //SOEF
    String[] userIds = new String[] { "r18EgyK_b" };
    userMessageInfos.setUserIds(userIds);
    userMessageInfos.execute(new GetMessageSubscriber());
  }

  public void createMessage(String data) {
    String[] userIds = new String[] { "r18EgyK_b" };
    createMessage.setParams(userIds, data);
    createMessage.execute(new CreateMessageSubscriber());
  }

  @Override public void onViewAttached(MVPView view) {
    chatMVPView = (ChatMVPView) view;
  }

  @Override public void onViewDetached() {
    userMessageInfos.unsubscribe();
    chatMVPView = null;
  }

  private class GetMessageSubscriber extends DefaultSubscriber<List<Message>> {

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

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      if (chatMVPView != null) chatMVPView.errorMessageCreation();
    }

    @Override public void onNext(Message message) {
      Timber.e("SUCESS SEND MESSAGE " + message.toString());
      if (chatMVPView != null) chatMVPView.successMessageCreated(message);
    }
  }
}
