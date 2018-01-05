package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

public class GetMessageSupport extends UseCase {

  private String lang;
  private String typeSupport;
  private ChatRepository chatRepository;

  @Inject
  public GetMessageSupport(CloudChatDataRepository chatRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.chatRepository = chatRepository;
  }

  public void setTypeSupport(String typeSupport) {
    this.typeSupport = typeSupport;
  }

  public void setup(String lang) {
    this.lang = lang;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.chatRepository.getMessageSupport(lang, typeSupport);
  }


}
