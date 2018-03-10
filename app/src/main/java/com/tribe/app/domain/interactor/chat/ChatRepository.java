package com.tribe.app.domain.interactor.chat;

import android.net.Uri;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.List;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

public interface ChatRepository {

  Observable<Message> createMessage(final String[] userIds, String type, String data, String gameId, String intent);

  Observable<List<Message>> loadMessages(final String[] userIds, String dateBefore,
      String dateAfter);

  Observable<List<Message>> getMessageZendesk();

  Observable<List<Message>> getMessagesImage(final String[] userIds);

  Observable<List<Message>> onMessageReceived();

  Observable<Message> onMessageRemoved();

  Observable<String> isTyping();

  Observable<String> isTalking();

  Observable<String> isReading();

  Observable<Boolean> imTyping(String[] userIds);

  Observable<List<Conversation>> getMessageSupport(String lang, String typeSupport);

  Observable addMessageZendesk(String typeMedia, String data, Uri uri);

  Observable createRequestZendesk(String data);
}
