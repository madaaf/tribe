package com.tribe.app.data.repository.chat.datasource;

import android.net.Uri;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.List;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

public interface ChatDataStore {

  Observable<MessageRealm> createMessage(final String[] userIds, String type, String data,
      String date, String gameId, String intent);

  Observable<UserRealm> loadMessages(final String[] userIds, String dateBefore, String dateAfter);

  Observable<List<MessageRealm>> getMessages(String[] userIds);

  Observable<List<MessageRealm>> getMessagesImage(String[] userIds);

  Observable<String> isTyping();

  Observable<String> isTalking();

  Observable<String> isReading();

  Observable<List<MessageRealm>> onMessageReceived();

  Observable<MessageRealm> onMessageRemoved();

  Observable<Boolean> imTyping(String[] userIds);

  Observable<List<Conversation>> getMessageSupport(String typeSupport);

  Observable<List<Message>> getMessageZendesk();

  Observable<Boolean> addMessageZendesk(String typeMedia, String data, Uri uri);

  Observable createRequestZendesk(String data);
}
