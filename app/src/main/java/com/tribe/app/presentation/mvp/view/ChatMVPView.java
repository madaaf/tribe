package com.tribe.app.presentation.mvp.view;

import com.tribe.app.presentation.view.widget.chat.Message;
import java.util.List;

/**
 * Created by madaaflak on 06/09/2017.
 */

public interface ChatMVPView extends MVPView {

  void successLoadingMessage(List<Message> messages);

  void errorLoadingMessage();
}
