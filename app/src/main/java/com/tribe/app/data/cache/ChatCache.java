package com.tribe.app.data.cache;

import com.tribe.app.presentation.view.widget.chat.Message;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

@Singleton public interface ChatCache {

  void messageCreated(Message message);

  Observable<Message> getMessageCreated();
}
