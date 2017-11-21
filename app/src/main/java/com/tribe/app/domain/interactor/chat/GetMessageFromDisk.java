package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by madaaflak on 14/09/2017.
 */

public class GetMessageFromDisk extends UseCaseDisk {

  private String[] userIds;
  private String dateBefore;
  private String dateAfter;
  private ChatRepository chatRepository;

  @Inject public GetMessageFromDisk(DiskChatDataRepository chatRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.chatRepository = chatRepository;
  }

  public void setUserIds(String[] userIds, String dateBefore, String dateAfter) {
    this.userIds = userIds;
    this.dateBefore = dateBefore;
    this.dateAfter = dateAfter;
  }

  @Override protected Observable buildUseCaseObservable() {
    return chatRepository.loadMessages(userIds, dateBefore, dateAfter);
  }
}
