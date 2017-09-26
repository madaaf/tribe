package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.chat.CloudChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

public class UserMessageInfos extends UseCase {

  private String[] userIds;
  private String date;
  private ChatRepository chatRepository;

  @Inject
  public UserMessageInfos(CloudChatDataRepository chatRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.chatRepository = chatRepository;
  }

  public void setUserIds(String[] userIds, String date) {
    this.userIds = userIds;
    this.date = date;
  }

  @Override protected Observable buildUseCaseObservable() {
    return this.chatRepository.loadMessages(userIds, date);
  }
}
