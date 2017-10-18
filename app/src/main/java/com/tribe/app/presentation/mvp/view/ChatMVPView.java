package com.tribe.app.presentation.mvp.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.List;

/**
 * Created by madaaflak on 06/09/2017.
 */

public abstract class ChatMVPView extends FrameLayout implements MVPView {

  public ChatMVPView(@NonNull Context context) {
    super(context);
  }

  public ChatMVPView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void successLoadingMessage(List<Message> messages) {
  }

  public void errorLoadingMessage() {
  }

  public void successLoadingMessageDisk(List<Message> messages) {
  }

  public void errorLoadingMessageDisk() {
  }

  public void successMessageCreated(Message message, int position) {
  }

  public void onShortcut(Shortcut shortcut) {
  }

  public void errorMessageCreation(int position) {
  }

  public void isTypingEvent(String userId) {
  }

  public void successShortcutUpdate(Shortcut shortcut) {
  }

  public void errorShortcutUpdate() {
  }

  public void successMessageReceived(List<Message> messages) {
  }

  public void errorMessageReveived() {
  }


}