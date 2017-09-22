package com.tribe.app.domain.interactor.chat;

import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.List;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

public interface ChatRepository {

  Observable<Message> createMessage(final String[] userIds, String type, String data);

  Observable<List<Message>> loadMessages(final String[] userIds);

  Observable<Message> createdMessages();
}
