package com.tribe.app.presentation.mvp.view;

import android.view.View;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.List;

/**
 * Created by madaaflak on 06/09/2017.
 */

public interface ChatMVPView extends MVPView {

  void successLoadingMessage(List<Message> messages);

  void errorLoadingMessage();

  void successLoadingMessageDisk(List<Message> messages);

  void errorLoadingMessageDisk();

  void successMessageCreated(Message message, View view);

  void errorMessageCreation();

  void successGetSubscribeMessage(Message message);

  void errorGetSubscribeMessage();
}
