package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.UserMessageInfos;
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

  @Inject public MessagePresenter(UserMessageInfos userMessageInfos) {
    this.userMessageInfos = userMessageInfos;
  }

  public void loadMessage() {
    userMessageInfos.execute(new GetMessageSubscriber());
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
}
