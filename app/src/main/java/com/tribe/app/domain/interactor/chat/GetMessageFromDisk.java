package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by madaaflak on 14/09/2017.
 */

public class GetMessageFromDisk extends UseCase {

  private String[] userIds;
  private ChatRepository chatRepository;

  @Inject
  public GetMessageFromDisk(DiskChatDataRepository chatRepository, ThreadExecutor threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.chatRepository = chatRepository;
  }

  public void setUserIds(String[] userIds) {
    this.userIds = userIds;
  }

  @Override protected Observable buildUseCaseObservable() {
    return chatRepository.loadMessages(userIds);
  }
}
