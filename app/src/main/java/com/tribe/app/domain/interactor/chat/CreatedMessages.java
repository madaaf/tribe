package com.tribe.app.domain.interactor.chat;

import com.tribe.app.data.repository.chat.DiskChatDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;
import javax.inject.Inject;
import rx.Observable;

/**
 * Created by madaaflak on 13/09/2017.
 */

public class CreatedMessages extends UseCaseDisk {

  private ChatRepository chatRepository;

  @Inject public CreatedMessages(DiskChatDataRepository chatRepository,
      PostExecutionThread postExecutionThread) {
    super(postExecutionThread);
    this.chatRepository = chatRepository;
  }

  @Override protected Observable buildUseCaseObservable() {
    return chatRepository.createdMessages();
  }
}
