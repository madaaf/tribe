package com.tribe.app.presentation.mvp.presenter;

import android.util.Pair;
import com.tribe.app.data.network.entity.RemoveMessageEntity;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.interactor.chat.CreateMessage;
import com.tribe.app.domain.interactor.chat.GetMessageFromDisk;
import com.tribe.app.domain.interactor.chat.GetMessageImageFromDisk;
import com.tribe.app.domain.interactor.chat.GetMessageSupport;
import com.tribe.app.domain.interactor.chat.ImTyping;
import com.tribe.app.domain.interactor.chat.IsReadingFromDisk;
import com.tribe.app.domain.interactor.chat.IsTalkingFromDisk;
import com.tribe.app.domain.interactor.chat.IsTypingFromDisk;
import com.tribe.app.domain.interactor.chat.OnMessageReceivedFromDisk;
import com.tribe.app.domain.interactor.chat.OnMessageRemovedFromDisk;
import com.tribe.app.domain.interactor.chat.RemoveMessage;
import com.tribe.app.domain.interactor.chat.UserMessageInfos;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.CreateShortcut;
import com.tribe.app.domain.interactor.user.GetDiskShortcut;
import com.tribe.app.domain.interactor.user.GetShortcutForUserIds;
import com.tribe.app.domain.interactor.user.UpdateShortcut;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.PictureMVPView;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by madaaflak on 06/09/2017.
 */

public class MessagePresenter implements Presenter {

  private ShortcutPresenter shortcutPresenter;

  // VIEW ATTACHED
  private ChatMVPView chatMVPView = null;
  private PictureMVPView pictureMVPView = null;

  // USECASES
  protected UserMessageInfos userMessageInfos;
  protected CreateMessage createMessage;
  protected GetMessageFromDisk getMessageFromDisk;
  protected GetMessageImageFromDisk getMessageImageFromDisk;
  protected GetDiskShortcut getDiskShortcut;
  protected IsTypingFromDisk isTypingFromDisk;
  protected IsTalkingFromDisk isTalkingFromDisk;
  protected IsReadingFromDisk isReadingFromDisk;
  protected OnMessageReceivedFromDisk onMessageReceivedFromDisk;
  protected OnMessageRemovedFromDisk onMessageRemovedFromDisk;
  protected GetMessageSupport getMessageSupport;
  protected ImTyping imTyping;
  protected CreateShortcut createShortcut;
  protected UpdateShortcut updateShortcut;
  protected GetShortcutForUserIds getShortcutForUserIds;
  protected RemoveMessage removeMessage;

  // SUBSCRIBERS
  private UpdateShortcutSubscriber updateShortcutSubscriber;
  private ShortcutForUserIdsSubscriber shortcutForUserIdsSubscriber;

  @Inject
  public MessagePresenter(ShortcutPresenter shortcutPresenter, UserMessageInfos userMessageInfos,
      CreateMessage createMessage, GetMessageFromDisk getMessageFromDisk,
      GetDiskShortcut getDiskShortcut, IsTypingFromDisk isTypingFromDisk, ImTyping imTyping,
      OnMessageReceivedFromDisk onMessageReceivedFromDisk, UpdateShortcut updateShortcut,
      GetMessageImageFromDisk getMessageImageFromDisk, GetShortcutForUserIds getShortcutForUserIds,
      CreateShortcut createShortcut, IsTalkingFromDisk isTalkingFromDisk,
      IsReadingFromDisk isReadingFromDisk, RemoveMessage removeMessage,
      OnMessageRemovedFromDisk onMessageRemovedFromDisk, GetMessageSupport getMessageSupport) {
    this.shortcutPresenter = shortcutPresenter;
    this.userMessageInfos = userMessageInfos;
    this.createMessage = createMessage;
    this.getMessageFromDisk = getMessageFromDisk;
    this.getDiskShortcut = getDiskShortcut;
    this.isTypingFromDisk = isTypingFromDisk;
    this.imTyping = imTyping;
    this.isTalkingFromDisk = isTalkingFromDisk;
    this.onMessageReceivedFromDisk = onMessageReceivedFromDisk;
    this.updateShortcut = updateShortcut;
    this.getMessageImageFromDisk = getMessageImageFromDisk;
    this.getShortcutForUserIds = getShortcutForUserIds;
    this.createShortcut = createShortcut;
    this.isReadingFromDisk = isReadingFromDisk;
    this.removeMessage = removeMessage;
    this.onMessageRemovedFromDisk = onMessageRemovedFromDisk;
    this.getMessageSupport = getMessageSupport;
  }

  public void getMessageImage(String[] userIds) {
    getMessageImageFromDisk.setUserIds(userIds);
    getMessageImageFromDisk.execute(new GetDiskMessageImageSubscriber());
  }

  public void removeMessage(Message m) {
    removeMessage.setup(m.getId());
    removeMessage.execute(new RemoveMessageSubscriber(m));
  }

  public void onMessageReceivedFromDisk() {
    onMessageReceivedFromDisk.execute(new GetDiskMessageReceivedSubscriber());
  }

  public void onMessageRemovedFromDisk() {
    onMessageRemovedFromDisk.execute(new GetDiskMessageRemovedSubscriber());
  }

  public void getMessageSupport() {
    getMessageSupport.execute(new GetDiskMessageSupportSubscriber());
  }

  public void quickShortcutForUserIds(String userIds) {
    if (shortcutForUserIdsSubscriber != null) {
      shortcutForUserIdsSubscriber.unsubscribe();
      shortcutForUserIdsSubscriber = null;
    }

    shortcutForUserIdsSubscriber = new ShortcutForUserIdsSubscriber(true, userIds);
    getShortcutForUserIds.setup(userIds);
    getShortcutForUserIds.execute(shortcutForUserIdsSubscriber);
  }

/*  public void getDiskShortcut(String shortcutId) {
    getDiskShortcut.setShortcutId(shortcutId);
    getDiskShortcut.execute(new GetDiskShortcutSubscriber());
  }*/

  public void imTypingMessage(String[] userIds) {
    imTyping.setUserIds(userIds);
    imTyping.execute(new DefaultSubscriber());
  }

  public void getIsTyping() {
    isTypingFromDisk.execute(new IsTypingDiskSubscriber(true));
  }

  public void getIsTalking() {
    isTalkingFromDisk.execute(new IsTypingDiskSubscriber(false));
  }

  public void getIsReading() {
    isReadingFromDisk.execute(new isReadingSubscriber());
  }

  public void loadMessagesDisk(String[] userIds, String dateBefore, String dateAfter) {
    getMessageFromDisk.setUserIds(userIds, dateBefore, dateAfter);
    getMessageFromDisk.execute(new LoadMessageDiskSubscriber());
  }

  public void loadMessage(String[] userIds, String dateBefore, String dateAfter) {
    userMessageInfos.setUserIds(userIds, dateBefore, dateAfter);
    userMessageInfos.execute(new LoadMessageSubscriber(dateAfter != null));
  }

  public void createMessage(String[] userIds, String data, String type, int positon) {
    createMessage.setParams(userIds, data, type);
    createMessage.execute(new CreateMessageSubscriber(positon));
  }

  public void updateShortcutName(String shortcutId, String name) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(ShortcutRealm.NAME, name));
    updateShortcut(shortcutId, values);
  }

  public void updateShortcutPicture(String shortcutId, String pictureUri) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(ShortcutRealm.PICTURE, pictureUri));
    updateShortcut(shortcutId, values);
  }

  private void updateShortcut(String shortcutId, List<Pair<String, String>> values) {
    updateShortcut.setup(shortcutId, values);
    updateShortcut.execute(new UpdateShortcutSubscriber());
  }

  private class UpdateShortcutSubscriber extends DefaultSubscriber<Shortcut> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Shortcut shortcut) {

    }
  }

  @Override public void onViewAttached(MVPView view) {
    if (view instanceof ChatMVPView) {
      chatMVPView = (ChatMVPView) view;
    } else if (view instanceof PictureMVPView) {
      pictureMVPView = (PictureMVPView) view;
    }
    shortcutPresenter.onViewAttached(view);
  }

  public boolean isAttached() {
    return (chatMVPView != null || pictureMVPView != null);
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    userMessageInfos.unsubscribe();
    createMessage.unsubscribe();
    getMessageFromDisk.unsubscribe();
    updateShortcut.unsubscribe();
    getDiskShortcut.unsubscribe();
    isTypingFromDisk.unsubscribe();
    onMessageReceivedFromDisk.unsubscribe();
    getShortcutForUserIds.unsubscribe();
    imTyping.unsubscribe();
    createShortcut.unsubscribe();
    if (shortcutForUserIdsSubscriber != null) shortcutForUserIdsSubscriber.unsubscribe();
    chatMVPView = null;
    pictureMVPView = null;
  }

  private class LoadMessageSubscriber extends DefaultSubscriber<List<Message>> {
    private boolean betweenTwoDate;

    public LoadMessageSubscriber(boolean betweenTwoDate) {
      this.betweenTwoDate = betweenTwoDate;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
      if (chatMVPView != null) chatMVPView.errorLoadingMessage();
    }

    @Override public void onNext(List<Message> messages) {
      if (chatMVPView != null) {
        if (!betweenTwoDate) {
          chatMVPView.successLoadingMessage(messages);
        } else {
          chatMVPView.successLoadingBetweenTwoDateMessage(messages);
        }
      }
    }
  }

  private class GetDiskMessageImageSubscriber extends DefaultSubscriber<List<Message>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
      if (pictureMVPView != null) pictureMVPView.errorGetMessageImageFromDisk();
    }

    @Override public void onNext(List<Message> messages) {
      if (pictureMVPView != null) pictureMVPView.successGetMessageImageFromDisk(messages);
    }
  }

  private class RemoveMessageSubscriber extends DefaultSubscriber<RemoveMessageEntity> {
    Message m;

    public RemoveMessageSubscriber(Message m) {
      this.m = m;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
      if (chatMVPView != null) chatMVPView.errorRemovedMessage(m);
    }

    @Override public void onNext(RemoveMessageEntity removeMessageEntity) {
      if (removeMessageEntity.isRemoved()) {
        if (chatMVPView != null) chatMVPView.successRemovedMessage(m);
      } else {
        if (chatMVPView != null) chatMVPView.errorRemovedMessage(m);
      }
    }
  }

  private class GetDiskMessageSupportSubscriber extends DefaultSubscriber<List<Conversation>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
    }

    @Override public void onNext(List<Conversation> conversations) {
      if (chatMVPView != null) chatMVPView.successMessageSupport(conversations.get(0).getMessages());
    }
  }

  private class GetDiskMessageRemovedSubscriber extends DefaultSubscriber<Message> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
      //if (chatMVPView != null) chatMVPView.errorRemovedMessage();
    }

    @Override public void onNext(Message message) {
      if (chatMVPView != null) {
        chatMVPView.successRemovedMessage(message);
      }
    }
  }

  private class GetDiskMessageReceivedSubscriber extends DefaultSubscriber<List<Message>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
      if (chatMVPView != null) chatMVPView.errorMessageReveived();
    }

    @Override public void onNext(List<Message> messages) {
      if (chatMVPView != null) chatMVPView.successMessageReceived(messages);
    }
  }

  private class isReadingSubscriber extends DefaultSubscriber<String> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
    }

    @Override public void onNext(String userId) {
      if (chatMVPView != null) {
        chatMVPView.isReadingUpdate(userId);
      }
    }
  }

  private class IsTypingDiskSubscriber extends DefaultSubscriber<String> {
    boolean typeEvent;

    public IsTypingDiskSubscriber(boolean typeEvent) {
      this.typeEvent = typeEvent;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
    }

    @Override public void onNext(String userId) {
      if (chatMVPView != null) chatMVPView.isTypingEvent(userId, typeEvent);
    }
  }

  private class LoadMessageDiskSubscriber extends DefaultSubscriber<List<Message>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.getMessage());
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
      if (chatMVPView != null) chatMVPView.errorMessageCreation(positon);
    }

    @Override public void onNext(Message message) {
      if (chatMVPView != null) chatMVPView.successMessageCreated(message, positon);
    }
  }

  public void createShortcut(boolean onQuickChat, String... userIds) {
    createShortcut.setup(userIds);
    createShortcut.execute(new ShortcutForUserIdsSubscriber(onQuickChat, userIds));
  }

  public void updateShortcutForUserIds(String... userIds) {
    getShortcutForUserIds.setup(userIds);
    getShortcutForUserIds.execute(new ShortcutForUserIdsSubscriber(false, userIds));
  }

  public void updateShortcutStatus(String shortcutId, @ShortcutRealm.ShortcutStatus String status) {
    shortcutPresenter.updateShortcutStatus(shortcutId, status, null);
  }

  private class ShortcutForUserIdsSubscriber extends DefaultSubscriber<Shortcut> {
    String[] userIds;
    boolean onQuickChat;

    public ShortcutForUserIdsSubscriber(boolean onQuickChat, String... userIds) {
      this.userIds = userIds;
      this.onQuickChat = onQuickChat;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Shortcut shortcut) {
      if (shortcut == null) {
        createShortcut(onQuickChat, userIds);
      } else {
        if (chatMVPView != null) {
          if (onQuickChat) {
            chatMVPView.onQuickShortcutUpdated(shortcut);
          } else {
            chatMVPView.onShortcutUpdate(shortcut);
          }
        }
      }

      if (shortcutForUserIdsSubscriber != null) shortcutForUserIdsSubscriber.unsubscribe();
      unsubscribe();
    }
  }
}
