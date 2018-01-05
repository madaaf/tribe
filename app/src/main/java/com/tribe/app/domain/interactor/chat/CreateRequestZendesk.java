package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

public class CreateRequestZendesk extends UseCase {

  private String data;
  private ChatRepository chatRepository;

  @Inject
  public CreateRequestZendesk(CloudChatDataRepository chatRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.chatRepository = chatRepository;
  }

  public void setData(String data) {
    this.data = data;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.chatRepository.createRequestZendesk(data);
  }
}
