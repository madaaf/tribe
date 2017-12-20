package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

public class GetMessageZendesk extends UseCase {

  private String lang;
  private int typeSupport;
  private String supportId;
  private ChatRepository chatRepository;

  @Inject
  public GetMessageZendesk(CloudChatDataRepository chatRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.chatRepository = chatRepository;
  }

  public void setTypeSupport(int typeSupport, String supportId) {
    this.typeSupport = typeSupport;
    this.supportId = supportId;
  }

  public void setup(String lang) {
    this.lang = lang;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.chatRepository.getMessageZendesk(lang, typeSupport, supportId);
  }
}
