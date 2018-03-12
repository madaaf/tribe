package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

public class CreateMessage extends UseCase {

  private String[] userIds;
  private String data;
  private String type;
  private String gameId;
  private String intent;
  private ChatRepository chatRepository;

  @Inject
  public CreateMessage(CloudChatDataRepository chatRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.chatRepository = chatRepository;
  }

  public void setParams(String[] userIds, String data, String type, String gameId, String intent) {
    this.userIds = userIds;
    this.data = data;
    this.type = type;
    this.gameId = gameId;
    this.intent = intent;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.chatRepository.createMessage(userIds, type, data, gameId, intent);
  }
}
