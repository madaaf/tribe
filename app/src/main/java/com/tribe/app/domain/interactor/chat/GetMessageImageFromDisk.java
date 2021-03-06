package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by madaaflak on 14/09/2017.
 */

public class GetMessageImageFromDisk extends UseCaseDisk {

  private String[] userIds;
  private ChatRepository chatRepository;

  @Inject public GetMessageImageFromDisk(DiskChatDataRepository chatRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.chatRepository = chatRepository;
  }

  public void setUserIds(String[] userIds) {
    this.userIds = userIds;
  }

  @Override protected Observable buildUseCaseObservable() {
    return chatRepository.getMessagesImage(userIds);
  }
}
