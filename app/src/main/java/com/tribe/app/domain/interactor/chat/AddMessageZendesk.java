package com.tribe.app.domain.interactor.chat;

import android.net.Uri;
import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

public class AddMessageZendesk extends UseCase {

  private Uri uri;
  private String data;
  private ChatRepository chatRepository;

  @Inject
  public AddMessageZendesk(CloudChatDataRepository chatRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.chatRepository = chatRepository;
  }

  public void setData(String data, Uri uri) {
    this.data = data;
    this.uri = uri;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.chatRepository.addMessageZendesk(data, uri);
  }
}
