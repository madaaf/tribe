package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.presentation.view.widget.chat.Message;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 12/09/2017.
 */

public class ChatCacheImpl implements ChatCache {

  private Context context;

  private PublishSubject<Message> onMessageCreated = PublishSubject.create();

  @Inject public ChatCacheImpl(Context context) {
    this.context = context;
  }

  @Override public void messageCreated(Message message) {
    onMessageCreated.onNext(message);
  }

  @Override public Observable<Message> getMessageCreated() {
    return onMessageCreated;
  }
}
