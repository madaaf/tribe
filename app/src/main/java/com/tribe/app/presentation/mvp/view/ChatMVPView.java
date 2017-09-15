package com.tribe.app.presentation.mvp.view;

import android.widget.ImageView;
import com.tribe.app.presentation.view.widget.chat.Message;
import java.util.List;

/**
 * Created by madaaflak on 06/09/2017.
 */

public interface ChatMVPView extends MVPView {

  void successLoadingMessage(List<Message> messages);

  void errorLoadingMessage();

  void successMessageCreated(Message message, ImageView imageView);

  void errorMessageCreation();

  void successGetSubscribeMessage(Message message);

  void errorGetSubscribeMessage();
}
