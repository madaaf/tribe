package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.interactor.chat.CreateMessage;
import com.tribe.app.domain.interactor.chat.GetMessageFromDisk;
import com.tribe.app.domain.interactor.chat.UserMessageInfos;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.GetDiskShortcut;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.view.widget.chat.model.Message;
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
  protected GetMessageFromDisk getMessageFromDisk;
  protected GetDiskShortcut getDiskShortcut;

  @Inject public MessagePresenter(UserMessageInfos userMessageInfos, CreateMessage createMessage,
      GetMessageFromDisk getMessageFromDisk, GetDiskShortcut getDiskShortcut) {
    this.userMessageInfos = userMessageInfos;
    this.createMessage = createMessage;
    this.getMessageFromDisk = getMessageFromDisk;
    this.getDiskShortcut = getDiskShortcut;
  }

  public void getDiskShortcut(String shortcutId) {
    getDiskShortcut.setShortcutId(shortcutId);
    getDiskShortcut.execute(new GetDiskShortcutSubscriber());
  }

  public void loadMessagesDisk(String[] userIds, String date) {
    getMessageFromDisk.setUserIds(userIds, date);
    getMessageFromDisk.execute(new LoadMessageDiskSubscriber());
  }

  public void loadMessage(String[] userIds, String date) {
    userMessageInfos.setUserIds(userIds, date);
    userMessageInfos.execute(new LoadMessageSubscriber());
  }

  public void createMessage(String[] userIds, String data, String type, int positon) {
    createMessage.setParams(userIds, data, type);
    createMessage.execute(new CreateMessageSubscriber(positon));
  }

  @Override public void onViewAttached(MVPView view) {
    chatMVPView = (ChatMVPView) view;
  }

  @Override public void onViewDetached() {
    userMessageInfos.unsubscribe();
    chatMVPView = null;
  }

  private class LoadMessageSubscriber extends DefaultSubscriber<List<Message>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
      if (chatMVPView != null) chatMVPView.errorLoadingMessage();
    }

    @Override public void onNext(List<Message> messages) {
      if (chatMVPView != null) chatMVPView.successLoadingMessage(messages);
    }
  }

  private class GetDiskShortcutSubscriber extends DefaultSubscriber<Shortcut> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      if (chatMVPView != null) chatMVPView.errorShortcutUpdate();
    }

    @Override public void onNext(Shortcut shortcuts) {
      if (chatMVPView != null) chatMVPView.successShortcutUpdate(shortcuts);
    }
  }

  private class LoadMessageDiskSubscriber extends DefaultSubscriber<List<Message>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      if (chatMVPView != null) chatMVPView.errorLoadingMessageDisk();
    }

    @Override public void onNext(List<Message> messages) {
      if (chatMVPView != null) chatMVPView.successLoadingMessageDisk(messages);
    }
  }

  private class CreateMessageSubscriber extends DefaultSubscriber<Message> {
    private int positon;

    public CreateMessageSubscriber(int positon) {
      this.positon = positon;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
      if (chatMVPView != null) chatMVPView.errorMessageCreation();
    }

    @Override public void onNext(Message message) {
      if (chatMVPView != null) chatMVPView.successMessageCreated(message, positon);
    }
  }
}
