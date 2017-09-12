package com.tribe.app.data.repository.chat;

import com.tribe.app.domain.interactor.chat.ChatRepository;
import com.tribe.app.presentation.view.widget.chat.Message;
import java.util.List;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

@Singleton public class DiskChatDataRepository implements ChatRepository {

  @Override public Observable<Message> createMessage(String[] userIds, String type, String data) {
    return null;
  }

  @Override public Observable<List<Message>> userMessageInfo(String[] userIds) {
    return null;
  }
}
